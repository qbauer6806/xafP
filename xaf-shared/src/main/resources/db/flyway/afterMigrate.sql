-- Grants (appelé après chaque migration)
-- A ne pas éditer, fichier maintenu par l'équipe DevOps

DO $$
BEGIN
  -- USAGE schema
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'admindev') THEN
    EXECUTE 'GRANT USAGE ON SCHEMA @@tscode@@ TO admindev';
    EXECUTE 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA @@tscode@@ TO admindev';
    EXECUTE 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA @@tscode@@ TO admindev';
  END IF;

  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'teamdev') THEN
    EXECUTE 'GRANT USAGE ON SCHEMA @@tscode@@ TO teamdev';
    EXECUTE 'GRANT SELECT ON ALL TABLES IN SCHEMA @@tscode@@ TO teamdev';
    EXECUTE 'GRANT SELECT ON ALL SEQUENCES IN SCHEMA @@tscode@@ TO teamdev';
  END IF;

  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '@@tscode@@') THEN
    EXECUTE 'GRANT USAGE ON SCHEMA @@tscode@@ TO @@tscode@@';
    EXECUTE 'GRANT INSERT, SELECT, UPDATE, DELETE ON ALL TABLES IN SCHEMA @@tscode@@ TO @@tscode@@';
    EXECUTE 'GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA @@tscode@@ TO @@tscode@@';
  END IF;

  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'statalltsetl') THEN
      EXECUTE 'GRANT USAGE ON SCHEMA @@tscode@@ TO statalltsetl';

      IF to_regclass('@@tscode@@.dem_statistiques') IS NOT NULL THEN
          EXECUTE 'GRANT SELECT ON @@tscode@@.dem_statistiques TO statalltsetl';
      END IF;

      IF to_regclass('@@tscode@@.dem_statistiques_demarches') IS NOT NULL THEN
          EXECUTE 'GRANT SELECT ON @@tscode@@.dem_statistiques_demarches TO statalltsetl';
      END IF;

      IF to_regclass('@@tscode@@.dem_statistiques_statuts') IS NOT NULL THEN
          EXECUTE 'GRANT SELECT ON @@tscode@@.dem_statistiques_statuts TO statalltsetl';
      END IF;

      IF to_regclass('@@tscode@@.dem_statistiques_types') IS NOT NULL THEN
          EXECUTE 'GRANT SELECT ON @@tscode@@.dem_statistiques_types TO statalltsetl';
      END IF;

      IF to_regclass('@@tscode@@.dem_statistiques_types_demarches') IS NOT NULL THEN
          EXECUTE 'GRANT SELECT ON @@tscode@@.dem_statistiques_types_demarches TO statalltsetl';
      END IF;
  END IF;
END $$;
