-- =====================
-- USER
-- user: admin, password: hashpw1
-- user: alice123, password: hashpw2
-- user: bob, password: hashpw3
-- user: charlie, password: hashpw4
-- =====================

INSERT INTO User (UserID, Username, Email, Password, Role, Status, Balance, PhoneNumber, isDeleted) VALUES
(1, 'admin', 'admin@example.com', '$2a$12$PK6G.ISl7CYUVHBZEdmtzOeuYyq317tKsu9eHiStEDjROdjRO/HhW', 'admin', 'active', 1000.00, '123-456-7890', FALSE),
(2, 'alice123', 'john.doe@example.com', '$2a$12$8iH7alLhKgxpYgGOwCyb6uldsSgRSZNjF/MWFKiudtJ/v2hSJpJnq', 'user', 'active', 50.00, '987-654-3210', FALSE),
(3, 'bob123', 'owner1@example.com', '$2a$12$FHBRZgpfEiLCnTOEkppIyuldqiewSaewi7oRYP9m0Sz7bBzbVOuU2', 'user, seller', 'active', 250.00, '555-123-4567', FALSE),
(4, 'charlie123', 'owner2@example.com', '$2a$12$rTVGGzmgLjNevXM1GM/aiua7LwDCafFNKUULuKHJ04gDrJoANOu6S', 'user, seller', 'active', 0.00, '555-987-6543', FALSE);

INSERT INTO Shop (ShopID, Name, Description, isDeleted, UserID, CreateBy) VALUES
                                                                              (1, 'Tech Gadgets', 'Your one-stop shop for the latest technology.', FALSE, 3, 3),
                                                                              (2, 'Home Decor Haven', 'Beautiful and unique items to decorate your home.', FALSE, 4, 4);

INSERT INTO Category (CategoryID, Name, Description, isDeleted, CreateBy) VALUES
                                                                              (1, 'Electronics', 'Smartphones, laptops, and other electronic devices.', FALSE, 1),
                                                                              (2, 'Home Goods', 'Furniture, decorations, and kitchenware.', FALSE, 1);

INSERT INTO Product (ProductID, Title, Description, Price, Quantity, isDeleted, ShopID, CategoryID, CreateBy) VALUES
                                                                                                                  (1, 'Wireless Mouse', 'Ergonomic mouse with a long-lasting battery.', 19.99, 150, FALSE, 1, 1, 3),
                                                                                                                  (2, '4K Smart TV', 'Stunning picture quality with built-in streaming apps.', 499.99, 50, FALSE, 1, 1, 3),
                                                                                                                  (3, 'Scented Candle Set', 'Set of 3 candles with various calming scents.', 25.00, 200, FALSE, 2, 2, 4),
                                                                                                                  (4, 'Wooden Bookshelf', 'Stylish and sturdy bookshelf, perfect for any room.', 120.50, 30, FALSE, 2, 2, 4);

INSERT INTO Discount (ID, DiscountPercent, StartDate, EndDate, isDeleted, ProductID, CreateBy) VALUES
                                                                                                   (1, 10.00, '2025-09-25', '2025-10-31', FALSE, 1, 1),
                                                                                                   (2, 20.00, '2025-10-01', '2025-10-15', FALSE, 3, 1);

INSERT INTO `Order` (OrderID, Quantity, TotalPrice, isDeleted, UserID, ProductID, CreateBy) VALUES
                                                                                                (1, 1, 499.99, FALSE, 2, 2, 2),
                                                                                                (2, 2, 50.00, FALSE, 2, 3, 2);

INSERT INTO Rating (ID, RatingPoint, Feedback, isDeleted, UserID, ProductID, CreateBy) VALUES
                                                                                           (1, 5, 'Great TV, amazing picture!', FALSE, 2, 2, 2),
                                                                                           (2, 4, 'Nice candles, smell wonderful.', FALSE, 2, 3, 2);

INSERT INTO Payouts (PayoutID, Amount, Status, TransactionFee, isDeleted, ShopID, CreateBy) VALUES
                                                                                                (1, 450.00, 'Completed', 5.00, FALSE, 1, 1),
                                                                                                (2, 45.00, 'Pending', 1.00, FALSE, 2, 1);

INSERT INTO Deposit (ID, Amount, PaymentMethod, Status, ActionType, isDeleted, UserID, CreateBy) VALUES
                                                                                                     (1, 100.00, 'Credit Card', 'Completed', 'Top-up', FALSE, 2, 2),
                                                                                                     (2, 50.00, 'PayPal', 'Completed', 'Top-up', FALSE, 3, 3);

INSERT INTO ActivityLog (ID, Action, Description, isDeleted, UserID, CreateBy) VALUES
                                                                                   (1, 'User registered', 'New user account created for johndoe', FALSE, 1, 1),
                                                                                   (2, 'Shop created', 'New shop "Tech Gadgets" was created', FALSE, 3, 3),
                                                                                   (3, 'Product added', 'Wireless Mouse was added to Tech Gadgets', FALSE, 3, 3);