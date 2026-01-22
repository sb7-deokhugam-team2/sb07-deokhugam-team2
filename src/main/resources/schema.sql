DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS popular_books CASCADE;
DROP TABLE IF EXISTS power_users CASCADE;
DROP TABLE IF EXISTS popular_reviews CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS liked_reviews CASCADE;

CREATE TABLE books
(
    id             UUID PRIMARY KEY,
    title          VARCHAR                  NOT NULL,
    author         VARCHAR                  NOT NULL,
    isbn           VARCHAR                  NOT NULL UNIQUE,
    published_date DATE                     NOT NULL,
    publisher      VARCHAR                  NOT NULL,
    created_at     TIMESTAMP with time zone NOT NULL,
    updated_at     TIMESTAMP with time zone NOT NULL,
    is_deleted     BOOLEAN                  NOT NULL,
    thumbnail_url  VARCHAR NULL,
    description    VARCHAR(1000)            NOT NULL
);

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR                  NOT NULL UNIQUE,
    nickname   VARCHAR                  NOT NULL,
    password   VARCHAR                  NOT NULL,
    is_deleted BOOLEAN DEFAULT false    NOT NULL,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone NOT NULL
);


CREATE TABLE reviews
(
    id          UUID PRIMARY KEY,
    rating      DOUBLE PRECISION         NOT NULL,
    content     VARCHAR(500)             NOT NULL,
    liked_count BIGINT  DEFAULT 0        NOT NULL,
    is_deleted  BOOLEAN DEFAULT false    NOT NULL,
    created_at  TIMESTAMP with time zone NOT NULL,
    updated_at  TIMESTAMP with time zone NOT NULL,
    user_id     UUID                     NOT NULL,
    book_id     UUID                     NOT NULL,
    CONSTRAINT fk_reviews_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_books FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT check_reviews_rating_gt_zero_and_loe_five CHECK ( rating > 0.0 AND rating <= 5.0),
    CONSTRAINT check_reviews_liked_count_goe_zero CHECK ( liked_count >= 0 )
);

CREATE TABLE popular_books
(
    id              UUID PRIMARY KEY,
    period_type     VARCHAR                  NOT NULL,
    calculated_date TIMESTAMP with time zone NOT NULL,
    rank            BIGINT                   NOT NULL,
    score           DOUBLE PRECISION         NOT NULL,
    created_at      TIMESTAMP with time zone NOT NULL,
    rating          DOUBLE PRECISION         NOT NULL,
    review_count    BIGINT                   NOT NULL,
    book_id         UUID                     NOT NULL,
    CONSTRAINT fk_popular_books_books FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT check_popular_books_period_type CHECK (period_type IN ('DAILY', 'MONTHLY', 'WEEKLY', 'ALL_TIME')),
    CONSTRAINT check_popular_books_review_count_gt_zero CHECK (review_count > 0),
    CONSTRAINT check_popular_books_rank_gt_zero CHECK ( rank > 0 ),
    CONSTRAINT check_popular_books_score_gt_zero CHECK ( score > 0 ),
    CONSTRAINT check_popular_books_rating_gt_zero_and_loe_five CHECK ( rating > 0.0 AND rating <= 5.0)
);

CREATE TABLE power_users
(
    id               UUID PRIMARY KEY,
    period_type      VARCHAR                  NOT NULL,
    calculated_date  TIMESTAMP with time zone NOT NULL,
    rank             BIGINT                   NOT NULL,
    score            DOUBLE PRECISION         NOT NULL,
    comment_count    BIGINT DEFAULT 0         NOT NULL,
    liked_count      BIGINT DEFAULT 0         NOT NULL,
    review_score_sum DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    created_at       TIMESTAMP with time zone NOT NULL,
    user_id          UUID                     NOT NULL,
    CONSTRAINT fk_power_users_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT check_power_users_period_type CHECK (period_type IN ('DAILY', 'MONTHLY', 'WEEKLY', 'ALL_TIME')),
    CONSTRAINT check_power_users_rank_gt_zero CHECK ( rank > 0 ),
    CONSTRAINT check_power_users_score_gt_zero CHECK ( score > 0 ),
    CONSTRAINT check_power_users_comment_count_goe_zero CHECK ( comment_count >= 0 ),
    CONSTRAINT check_power_users_liked_count_goe_zero CHECK ( liked_count >= 0 ),
    CONSTRAINT check_power_users_review_score_sum_goe_zero CHECK ( review_score_sum >= 0 )
);


CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    content    varchar(500)             NOT NULL,
    is_deleted BOOLEAN DEFAULT false    NOT NULL,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone NOT NULL,
    review_id  UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_comments_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    content    VARCHAR NULL,
    confirmed  BOOLEAN DEFAULT false    NOT NULL,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone NOT NULL,
    review_id  UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_notifications_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE popular_reviews
(
    id              UUID PRIMARY KEY,
    period_type     VARCHAR                  NOT NULL,
    calculated_date TIMESTAMP with time zone NOT NULL,
    rank            BIGINT                   NOT NULL,
    score           DOUBLE PRECISION         NOT NULL,
    created_at      TIMESTAMP with time zone NOT NULL,
    liked_count     BIGINT DEFAULT 0         NOT NULL,
    comment_count   BIGINT DEFAULT 0         NOT NULL,
    review_id       UUID                     NOT NULL,
    CONSTRAINT fk_popular_reviews_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT check_popular_reviews_period_type CHECK (period_type IN ('DAILY', 'MONTHLY', 'WEEKLY', 'ALL_TIME')),
    CONSTRAINT check_popular_reviews_rank_goe_zero CHECK ( rank > 0 ),
    CONSTRAINT check_popular_reviews_score_goe_zero CHECK ( score > 0 ),
    CONSTRAINT check_popular_reviews_liked_count_goe_zero CHECK ( liked_count >= 0 ),
    CONSTRAINT check_popular_reviews_comment_count_goe_zero CHECK ( comment_count >= 0 )
);

CREATE TABLE liked_reviews
(
    id         UUID PRIMARY KEY,
    liked      BOOLEAN DEFAULT true     NOT NULL,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone NOT NULL,
    review_id  UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_liked_reviews_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_liked_reviews_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_liked_reviews_review_user UNIQUE (review_id, user_id)
);