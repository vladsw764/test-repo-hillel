CREATE TABLE automobile
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(50),
    color             VARCHAR(50),
    creation_date     TIMESTAMP,
    update_date       TIMESTAMP,
    is_original_color BOOLEAN DEFAULT true,
    deleted           BOOLEAN DEFAULT false
);
