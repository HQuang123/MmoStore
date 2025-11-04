-- USER
CREATE TABLE User (
                      UserID                INT PRIMARY KEY AUTO_INCREMENT,
                      Name                  VARCHAR(100) NOT NULL,
                      Email                     VARCHAR(100) NOT NULL,
                      Password                  VARCHAR(255) NOT NULL,
                      Role                      VARCHAR(50),
                      Status                    BOOLEAN DEFAULT TRUE,   -- TRUE = active, FALSE = inactive
                      Balance                   DECIMAL(15,2) DEFAULT 0,
                      PhoneNumber               VARCHAR(20),
                      ProfileImage              VARCHAR(500),
                      AccountStatusNonLocked    BOOLEAN DEFAULT TRUE,
                      AccountFailedAttemptCount INT DEFAULT 0,
                      AccountLockTime           DATETIME NULL,
                      ResetTokens               VARCHAR(255),
                      isDeleted                 BOOLEAN DEFAULT FALSE,
                      CreateAt                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CreateBy                  INT,
                      UpdateAt                  TIMESTAMP NULL,
                      UpdateBy                  INT
);


-- SHOP
CREATE TABLE Shop (
                      ShopID     INT PRIMARY KEY AUTO_INCREMENT,
                      Name       VARCHAR(255) NOT NULL,
                      Description TEXT,
                      ShopImageUrl VARCHAR(255),
                      isDeleted  BOOLEAN DEFAULT FALSE,
                      CreateAt   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CreateBy   INT,
                      UpdateAt   TIMESTAMP NULL,
                      UpdateBy   INT,
                      UserID     INT
);

-- CATEGORY
CREATE TABLE Category (
                          CategoryID  INT PRIMARY KEY AUTO_INCREMENT,
                          Name        VARCHAR(255) NOT NULL,
                          Description TEXT,
                          CategoryImageUrl VARCHAR(255),
                          isDeleted   BOOLEAN DEFAULT FALSE,
                          CreateAt    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CreateBy    INT,
                          UpdateAt    TIMESTAMP NULL,
                          UpdateBy    INT
);

-- PRODUCT
CREATE TABLE Product (
                         ProductID   INT PRIMARY KEY AUTO_INCREMENT,
                         Title       VARCHAR(255) NOT NULL,
                         Description TEXT,
                         Price       DECIMAL(15,2) NOT NULL,
                         Quantity    INT NOT NULL,
                         isDeleted   BOOLEAN DEFAULT FALSE,
                         ProductImageUrl VARCHAR(255),
                         CreateAt    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CreateBy    INT,
                         UpdateAt    TIMESTAMP NULL,
                         UpdateBy    INT,
                         ShopID      INT,
                         CategoryID  INT,
                         FOREIGN KEY (ShopID)     REFERENCES Shop(ShopID),
                         FOREIGN KEY (CategoryID) REFERENCES Category(CategoryID)
);

-- DISCOUNT
CREATE TABLE Discount (
                          ID              INT PRIMARY KEY AUTO_INCREMENT,
                          DiscountPercent DECIMAL(5,2) NOT NULL,
                          StartDate       DATE,
                          EndDate         DATE,
                          isDeleted       BOOLEAN DEFAULT FALSE,
                          CreateAt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CreateBy        INT,
                          UpdateAt        TIMESTAMP NULL,
                          UpdateBy        INT,
                          ProductID       INT,
                          FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

-- ORDER
CREATE TABLE `Orders` (
                          OrderID    INT PRIMARY KEY AUTO_INCREMENT,
                          Quantity   INT NOT NULL,
                          TotalPrice DECIMAL(15,2) NOT NULL,
                          isDeleted  BOOLEAN DEFAULT FALSE,
                          Status     VARCHAR(255),
                          Message    NVARCHAR(255),
                          CreateAt   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CreateBy   INT,
                          UpdateAt   TIMESTAMP NULL,
                          UpdateBy   INT,
                          UserID     INT,
                          ProductID  INT,
                          FOREIGN KEY (UserID)    REFERENCES User(UserID),
                          FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

-- RATING
CREATE TABLE Rating (
                        ID          INT PRIMARY KEY AUTO_INCREMENT,
                        RatingPoint INT CHECK (RatingPoint BETWEEN 1 AND 5),
                        Feedback    TEXT,
                        isDeleted   BOOLEAN DEFAULT FALSE,
                        CreateAt    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CreateBy    INT,
                        UpdateAt    TIMESTAMP NULL,
                        UpdateBy    INT,
                        UserID      INT,
                        ProductID   INT,
                        FOREIGN KEY (UserID)    REFERENCES User(UserID),
                        FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

-- PAYOUTS
CREATE TABLE Payouts (
                         PayoutID       INT PRIMARY KEY AUTO_INCREMENT,
                         Amount         DECIMAL(15,2) NOT NULL,
                         Status         VARCHAR(50),
                         TransactionFee DECIMAL(15,2),
                         isDeleted      BOOLEAN DEFAULT FALSE,
                         CreateAt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CreateBy       INT,
                         UpdateAt       TIMESTAMP NULL,
                         UpdateBy       INT,
                         ShopID         INT,
                         FOREIGN KEY (ShopID)   REFERENCES Shop(ShopID)
);

-- DEPOSIT
CREATE TABLE Deposit (
                         ID            INT PRIMARY KEY AUTO_INCREMENT,
                         Amount        DECIMAL(15,2) NOT NULL,
                         PaymentMethod VARCHAR(100),
                         Status        VARCHAR(50),
                         ActionType    VARCHAR(50),
                         isDeleted     BOOLEAN DEFAULT FALSE,
                         CreateAt      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CreateBy      INT,
                         UpdateAt      TIMESTAMP NULL,
                         UpdateBy      INT,
                         UserID        INT,
                         FOREIGN KEY (UserID)   REFERENCES User(UserID)
);

-- ACTIVITY LOG
CREATE TABLE ActivityLog (
                             ID          INT PRIMARY KEY AUTO_INCREMENT,
                             Action      VARCHAR(255),
                             Description TEXT,
                             isDeleted   BOOLEAN DEFAULT FALSE,
                             CreateAt    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CreateBy    INT,
                             UpdateAt    TIMESTAMP NULL,
                             UpdateBy    INT,
                             UserID      INT,
                             FOREIGN KEY (UserID)   REFERENCES User(UserID)
);

CREATE TABLE Withdrawal (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            Amount DECIMAL(15,2) NOT NULL,
                            Bank VARCHAR(50) NOT NULL,
                            BankAccount VARCHAR(50) NOT NULL,
                            AccountHolder VARCHAR(100) NOT NULL,
                            Status VARCHAR(20) DEFAULT 'Pending',
                            CreateAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CreateBy INT, -- User ID of the creator
                            UpdateAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UpdateBy INT, -- User ID of the updater
                            UserID INT NOT NULL,
                            FOREIGN KEY (UserID) REFERENCES User(UserID)
);

CREATE TABLE `Notifications` (
                                 `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 `UserId` INT NOT NULL,
                                 `title` VARCHAR(255) NOT NULL,
                                 `content` TEXT,
                                 `Status` VARCHAR(20) DEFAULT 'Unread',
                                 `CreateAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 `UpdateAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `CreateBy` INT,
                                 `DeleteBy` INT,
                                 `IsDeleted` TINYINT(1) DEFAULT 0, -- Using 0 for false
                                 FOREIGN KEY (`UserId`) REFERENCES `User`(`UserID`),

                                 FOREIGN KEY (`CreateBy`) REFERENCES `User`(`UserId`),
                                 FOREIGN KEY (`DeleteBy`) REFERENCES `User`(`UserID`)
);

CREATE TABLE VerificationToken (
                                    ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                    Token VARCHAR(255) NOT NULL UNIQUE,
                                    ExpiryDate DATETIME NOT NULL,
                                    UserID INT NOT NULL UNIQUE,
                                    FOREIGN KEY (UserID) REFERENCES User(UserID)
);
CREATE TABLE PasswordResetToken (
                                   ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                   Token VARCHAR(255) NOT NULL UNIQUE,
                                   ExpiryDate DATETIME NOT NULL,
                                   UserID INT NOT NULL UNIQUE,
                                   FOREIGN KEY (UserID) REFERENCES User(UserID)
);
CREATE TABLE WithdrawalOtp (
                               ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               Token VARCHAR(255) NOT NULL UNIQUE,
                               ExpiryDate DATETIME NOT NULL,
                               WithdrawalID INT NOT NULL UNIQUE,
                               FOREIGN KEY (WithdrawalID) REFERENCES Withdrawal(ID)
);

