ALTER TABLE ten_man_poll
    ADD COLUMN locked_date_option_id BIGINT REFERENCES ten_man_date_option(id);
