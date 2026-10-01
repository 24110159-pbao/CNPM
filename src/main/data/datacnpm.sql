CREATE DATABASE cnpm;

INSERT INTO users
(name, email, password, role, created_at, updated_at)
VALUES
(
    'Admin',
    'admin@gmail.com',
    '$2a$10$u.ZUTgmnqFdx4s5JyT8pKuYpsR4xlXwh3lGrgLXmU9dhasMBw/eA.',
    'MANAGER',
    NOW(),
    NOW()
);

INSERT INTO users
(name, email, password, role, created_at, updated_at)
VALUES
(
    'phibao',
    '24110159@student.hcmute.edu.vn',
    '$2a$10$u.ZUTgmnqFdx4s5JyT8pKuYpsR4xlXwh3lGrgLXmU9dhasMBw/eA.',
    'USER',
    NOW(),
    NOW()
);