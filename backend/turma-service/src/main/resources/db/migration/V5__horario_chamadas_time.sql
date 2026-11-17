-- Garante que chamadas.horario seja TIME (e NOT NULL), igual ao LocalTime da aplicação.
-- Junto com sendTimeAsDatetime=false (application.properties), resolve o erro
-- "The data types time and datetime are incompatible in the equal to operator" ao criar chamada.
-- A coluna participa da restrição única (turma_id, data, horario): o SQL Server não deixa alterar
-- uma coluna usada por restrição, então ela é removida, a coluna é alterada e a restrição é recriada.

IF EXISTS (SELECT 1 FROM sys.key_constraints
           WHERE name = 'uq_chamadas_turma_data_horario' AND parent_object_id = OBJECT_ID('chamadas'))
    ALTER TABLE chamadas DROP CONSTRAINT uq_chamadas_turma_data_horario;

ALTER TABLE chamadas ALTER COLUMN horario TIME NOT NULL;

ALTER TABLE chamadas ADD CONSTRAINT uq_chamadas_turma_data_horario UNIQUE (turma_id, data, horario);
