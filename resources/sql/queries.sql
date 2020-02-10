-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id




-- :name create-message! :! :n
-- :doc create a static message to be sent on a given exchange/queue
INSERT INTO messages
(id, msg_type, pb_type, exchange, queue, content)
VALUES (:id, :msg_type, :pb_type, :exchange, :queue, :content)


-- :name get-messages :? :*
-- :doc get all messages from the messages table
SELECT * FROM messages


-- :name get-message :? :1
-- :doc get the message with the given :id
SELECT * FROM messages
WHERE id = :id


-- :name get-messages-by-type :? :*
-- :doc get all the messages of a given :type
SELECT * FROM messages
WHERE msg_type = :msg_type


-- :name get-messages-by-pb-type :? :*
-- :doc get all the messages of a given :type
SELECT * FROM messages
WHERE pb_type = :pb_type


-- :name clear-messages! :! :*
-- :doc remove all messages form the database
DELETE FROM messages



-- :name create-consumer! :! :n
-- :doc create "listener" on a given exchange/queue
INSERT INTO consumers
(id, msg_type, pb_type, exchange, queue)
VALUES (:id, :msg_type, :pb_type, :exchange, :queue)


-- :name get-consumers :? :*
-- :doc get all consumers from the consumers table
SELECT * FROM consumers


-- :name get-consumer :? :1
-- :doc get the consumer with the given :id
SELECT * FROM consumers
WHERE id = :id


-- :name get-consumers-by-type :? :*
-- :doc get all theconsumers of a given :type
SELECT * FROM consumers
WHERE msg_type = :msg_type


-- :name get-consumers-by-pb-type :? :*
-- :doc get all the consumers of a given :type
SELECT * FROM consumers
WHERE pb_type = :pb_type


-- :name clear-consumers! :! :*
-- :doc remove all consumers form the database
DELETE FROM consumers



