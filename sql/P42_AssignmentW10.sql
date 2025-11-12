/* ================================
   P42_AssignmentW10 — FULL SCHEMA
   SQL Server (T-SQL)
   ================================ */

-- 0) Tạo mới DB an toàn
IF DB_ID('P42_AsignmentW10') IS NOT NULL
BEGIN
    ALTER DATABASE P42_AsignmentW10 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE P42_AsignmentW10;
END;
GO
CREATE DATABASE P42_AsignmentW10;
GO
USE P42_AsignmentW10;
GO

/* ========== 1) Lookup: Departments, Roles, LeaveTypes ========== */
CREATE TABLE dbo.Departments (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE dbo.Roles (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    code         NVARCHAR(30)  NOT NULL UNIQUE,   -- EMPLOYEE / MANAGER / LEADER
    name         NVARCHAR(100) NOT NULL
);

CREATE TABLE dbo.LeaveTypes (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    code               NVARCHAR(30)  NOT NULL UNIQUE,  -- ANNUAL / SICK / MARRIAGE / ...
    name               NVARCHAR(100) NOT NULL,
    requires_document  BIT NOT NULL DEFAULT (0)
);

/* ========== 2) Users (chuẩn hoá, có manager_id) ========== */
CREATE TABLE dbo.Users (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    username      NVARCHAR(50)  NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,          -- (Gợi ý: lưu hash, không lưu plain)
    full_name     NVARCHAR(120) NOT NULL,
    role_id       INT NOT NULL FOREIGN KEY REFERENCES dbo.Roles(id),
    department_id INT NOT NULL FOREIGN KEY REFERENCES dbo.Departments(id),
    manager_id    INT NULL     FOREIGN KEY REFERENCES dbo.Users(id),
    is_active     BIT NOT NULL DEFAULT (1),
    created_at    DATETIME2(0) NOT NULL DEFAULT (SYSUTCDATETIME())
);
CREATE INDEX IX_Users_Manager ON dbo.Users(manager_id);

/* ========== 3) Requests + History + Attachments ========== */
CREATE TABLE dbo.Requests (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    employee_id    INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(id),
    type_id        INT NOT NULL FOREIGN KEY REFERENCES dbo.LeaveTypes(id),

    title          NVARCHAR(100) NOT NULL,
    reason         NVARCHAR(255) NOT NULL,

    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,

    status         NVARCHAR(20) NOT NULL 
        CONSTRAINT CK_Requests_Status CHECK (status IN (N'INPROGRESS', N'APPROVED', N'REJECTED')),

    manager_note   NVARCHAR(255) NULL,

    created_at     DATETIME2(0) NOT NULL DEFAULT (SYSUTCDATETIME()),
    created_by     INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(id),

    processed_at   DATETIME2(0) NULL,
    processed_by   INT NULL FOREIGN KEY REFERENCES dbo.Users(id),

    -- Duration (ngày) để query nhanh (bao gồm cả 2 đầu)
    duration_days  AS (DATEDIFF(DAY, start_date, end_date) + 1) PERSISTED
);

-- Ràng buộc ngày hợp lệ
ALTER TABLE dbo.Requests ADD CONSTRAINT CK_Requests_DateRange
CHECK (start_date <= end_date);

-- Chỉ mục phục vụ màn hình list/agenda/duyệt
CREATE INDEX IX_Requests_EmployeeStatus ON dbo.Requests(employee_id, status);
CREATE INDEX IX_Requests_DateRange     ON dbo.Requests(start_date, end_date);
CREATE INDEX IX_Requests_ProcessedBy   ON dbo.Requests(processed_by);

-- Lịch sử chuyển trạng thái / ghi chú duyệt
CREATE TABLE dbo.RequestHistory (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    request_id   INT NOT NULL FOREIGN KEY REFERENCES dbo.Requests(id),
    old_status   NVARCHAR(20) NULL,
    new_status   NVARCHAR(20) NOT NULL,
    changed_by   INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(id),
    changed_at   DATETIME2(0) NOT NULL DEFAULT (SYSUTCDATETIME()),
    note         NVARCHAR(255) NULL
);
CREATE INDEX IX_History_Request ON dbo.RequestHistory(request_id);

-- Đính kèm minh chứng
CREATE TABLE dbo.Attachments (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    request_id   INT NOT NULL FOREIGN KEY REFERENCES dbo.Requests(id),
    file_name    NVARCHAR(255) NOT NULL,
    file_path    NVARCHAR(500) NOT NULL,  -- đường dẫn lưu file
    uploaded_by  INT NOT NULL FOREIGN KEY REFERENCES dbo.Users(id),
    uploaded_at  DATETIME2(0) NOT NULL DEFAULT (SYSUTCDATETIME())
);
CREATE INDEX IX_Attachments_Request ON dbo.Attachments(request_id);

/* ========== 4) Trigger: tự ghi lịch sử khi đổi trạng thái/duyệt ========== */
GO
CREATE OR ALTER TRIGGER dbo.tr_Requests_StatusHistory
ON dbo.Requests
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Ghi lịch sử khi status/manager_note/processed_by/processed_at thay đổi
    INSERT INTO dbo.RequestHistory (request_id, old_status, new_status, changed_by, note, changed_at)
    SELECT
        i.id,
        d.status,
        i.status,
        ISNULL(i.processed_by, i.created_by),  -- nếu chưa có processed_by thì tạm ghi người tạo
        i.manager_note,
        SYSUTCDATETIME()
    FROM inserted i
    JOIN deleted  d ON d.id = i.id
    WHERE (ISNULL(d.status,'') <> ISNULL(i.status,''))
       OR (ISNULL(d.manager_note,'') <> ISNULL(i.manager_note,''))
       OR (ISNULL(d.processed_by,0) <> ISNULL(i.processed_by,0))
       OR (ISNULL(d.processed_at,'1900-01-01') <> ISNULL(i.processed_at,'1900-01-01'));
END;
GO

/* ========== 5) (Tuỳ chọn) View phục vụ Agenda: mỗi ngày 1 dòng ========== */
/* Dùng cho lịch phòng ban: ngày nào nhân viên OFF (đã APPROVED) */
CREATE OR ALTER VIEW dbo.vw_Agenda
AS
SELECT 
    u.id            AS user_id,
    u.full_name,
    d.name          AS department,
    r.id            AS request_id,
    r.status,
    dt.work_date
FROM dbo.Users u
JOIN dbo.Departments d ON d.id = u.department_id
JOIN dbo.Requests   r ON r.employee_id = u.id AND r.status = N'APPROVED'
CROSS APPLY (
    SELECT DATEADD(DAY, v.number, r.start_date) AS work_date
    FROM master..spt_values v
    WHERE v.type = 'P'
      AND DATEADD(DAY, v.number, r.start_date) <= r.end_date
) dt;
GO

/* ========== 6) SEED dữ liệu (map theo seed bạn đưa) ========== */
-- Departments
INSERT INTO dbo.Departments(name) VALUES (N'IT'), (N'QA');

-- Roles
INSERT INTO dbo.Roles(code, name) VALUES
(N'EMPLOYEE', N'Nhân viên'),
(N'MANAGER',  N'Quản lý'),
(N'LEADER',   N'Trưởng nhóm');

-- LeaveTypes
INSERT INTO dbo.LeaveTypes(code, name, requires_document) VALUES
(N'ANNUAL',   N'Phép năm', 0),
(N'SICK',     N'Nghỉ ốm',  1),
(N'MARRIAGE', N'Nghỉ cưới',1);

-- Users (dùng '123' tạm; khuyến nghị thay bằng hash)
DECLARE @IT INT = (SELECT id FROM dbo.Departments WHERE name=N'IT');
DECLARE @QA INT = (SELECT id FROM dbo.Departments WHERE name=N'QA');

DECLARE @EMP INT = (SELECT id FROM dbo.Roles WHERE code=N'EMPLOYEE');
DECLARE @MAN INT = (SELECT id FROM dbo.Roles WHERE code=N'MANAGER');
DECLARE @LEA INT = (SELECT id FROM dbo.Roles WHERE code=N'LEADER');

-- Tạo trước manager để gán manager_id
INSERT INTO dbo.Users(username,password_hash,full_name,role_id,department_id,manager_id)
VALUES
(N'bob',  N'123', N'Bob Tran',  @MAN, @IT, NULL),  -- Manager IT
(N'mike', N'123', N'Mike Le',   @MAN, @QA, NULL);  -- Manager QA

-- Lấy id của 2 manager
DECLARE @bob  INT = (SELECT id FROM dbo.Users WHERE username=N'bob');
DECLARE @mike INT = (SELECT id FROM dbo.Users WHERE username=N'mike');

-- Nhân viên/Leader báo cáo lên manager
INSERT INTO dbo.Users(username,password_hash,full_name,role_id,department_id,manager_id)
VALUES
(N'alice', N'123', N'Alice Nguyen', @EMP, @IT,  @bob),
(N'carl',  N'123', N'Carl Pham',    @LEA, @IT,  @bob),
(N'eva',   N'123', N'Eva Do',       @EMP, @QA,  @mike);

-- Một vài Requests mẫu
DECLARE @ANNUAL INT = (SELECT id FROM dbo.LeaveTypes WHERE code=N'ANNUAL');
DECLARE @SICK   INT = (SELECT id FROM dbo.LeaveTypes WHERE code=N'SICK');

DECLARE @alice INT = (SELECT id FROM dbo.Users WHERE username=N'alice');
DECLARE @carl  INT = (SELECT id FROM dbo.Users WHERE username=N'carl');
DECLARE @eva   INT = (SELECT id FROM dbo.Users WHERE username=N'eva');

-- INPROGRESS (chưa duyệt)
INSERT INTO dbo.Requests (
    employee_id, type_id, title, reason, start_date, end_date, status, created_by
) VALUES
(@alice, @ANNUAL, N'Nghỉ phép năm', N'Về quê',    '2025-10-20', '2025-10-22', N'INPROGRESS', @alice);

-- APPROVED (đã duyệt)
INSERT INTO dbo.Requests (
    employee_id, type_id, title, reason, start_date, end_date, status, manager_note, created_by, processed_by, processed_at
) VALUES
(@carl,  @SICK,   N'Nghỉ ốm',      N'Cảm cúm',    '2025-10-15', '2025-10-16', N'APPROVED',  N'Chúc mau khoẻ', @carl,  @bob,  SYSUTCDATETIME()),
(@eva,   @ANNUAL, N'Nghỉ phép năm', N'Việc gia đình','2025-10-25','2025-10-27',N'APPROVED', N'OK',            @eva,   @mike, SYSUTCDATETIME());

/* ========== 7) (Tuỳ chọn) View tương thích Users_Flat (nếu code cũ dùng role/department dạng text) ========== */
CREATE OR ALTER VIEW dbo.Users_Flat
AS
SELECT 
    u.id, u.username, u.full_name, u.password_hash,
    r.code  AS role_code,
    r.name  AS role_name,
    d.name  AS department_name,
    u.manager_id, u.is_active, u.created_at
FROM dbo.Users u
JOIN dbo.Roles r ON r.id = u.role_id
JOIN dbo.Departments d ON d.id = u.department_id;
GO

/* ========== 8) Gợi ý API/logic (tham khảo) ==========
- Đăng nhập: Users.username + password_hash (hash thực), lấy role_code/department.
- Tạo đơn: INSERT Requests (status = INPROGRESS, created_by = employee_id).
- Duyệt: UPDATE Requests SET status='APPROVED'|'REJECTED', manager_note=?, processed_by=?, processed_at=GETUTCDATE().
         -> Trigger sẽ tự ghi RequestHistory.
- Agenda: SELECT * FROM vw_Agenda WHERE department = N'IT' AND work_date BETWEEN @from AND @to.
====================================================== */


GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER VIEW dbo.vw_Agenda
AS
SELECT 
    u.id  AS user_id,
    u.full_name,
    d.name AS department,
    r.id  AS request_id,
    r.status,
    DATEADD(DAY, t.n, r.start_date) AS work_date
FROM dbo.Users u
JOIN dbo.Departments d ON d.id = u.department_id
JOIN dbo.Requests r     ON r.employee_id = u.id
                        AND r.status = N'APPROVED'
CROSS APPLY (
    /* tạo dãy 0..(số ngày nghỉ-1) mà không phụ thuộc master..spt_values */
    SELECT TOP (DATEDIFF(DAY, r.start_date, r.end_date) + 1)
           ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS n
    FROM sys.all_objects AS a
) AS t;
GO
