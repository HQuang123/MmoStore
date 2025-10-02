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
