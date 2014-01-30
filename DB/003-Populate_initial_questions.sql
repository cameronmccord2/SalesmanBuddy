-- Types
-- 1:Image
-- 2:BOOL
-- 3:Text
-- 4:Dropdown
-- 5:Save Cancel button

-- Tags
-- 1:License Image
-- 2:Stock Number
-- 3:First Name
-- 4:Last Name
-- 5:Phone Number
-- 6:Sold the car

INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (1, 'License Image', 'Spanish license image', 1, 1, 1);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (2, 'Stock Number', 'Spanish stock number', 3, 0, 2);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (3, 'What is the insurance provider of the buyer', 'Spanish What is the insurance provider of the buyer', 3, 1, 0);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (4, 'What is the name of the insurance agent of the buyer', 'Spanish What is the name of the insurance agent of the buyer', 3, 1, 0);
-- Contact Info Questions
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (5, 'First Name', 'Nombre Priero', 3, 0, 3);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (6, 'Last Name', 'Nombre Apeido', 3, 0, 4);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (7, 'Phone Number', 'Numero del telephono', 3, 0, 5);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (8, 'Address', 'Domocilio', 3, 0, 0);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (9, 'City', 'Ciudad', 3, 0, 0);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (10, 'State', 'Spanish state', 3, 0, 0);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (11, 'Notes', 'Spanish notes', 3, 0, 0);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (12, 'Sold this car to this buyer', 'Spanish sold this car to this buyer', 2, 0, 6);
INSERT INTO questions (questionOrder, questionTextEnglish, questionTextSpanish, questionType, required, tag) VALUES (13, '', '', 5, 0, 0);