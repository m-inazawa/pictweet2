ALTER TABLE tweets ADD COLUMN user_id INT;

ALTER TABLE tweets ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);

-- ADD CONSTRAINTで制約を追加
-- FOREIGN KEY (user_id) REFERENCES users(id)：user_idカラムに、usersテーブルのidの外部キーを保存するという設定


