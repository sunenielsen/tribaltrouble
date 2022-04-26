USE oddlabs;

INSERT INTO registrations (reg_key, reg_time) SELECT 'H2MU-576N-DJ58-W8Q6', now() FROM DUAL WHERE NOT EXISTS (SELECT reg_key FROM registrations WHERE reg_key = 'H2MU-576N-DJ58-W8Q6');
INSERT INTO registrations (reg_key, reg_time) SELECT 'K6AA-Y33X-C7ZT-K4TF', now() FROM DUAL WHERE NOT EXISTS (SELECT reg_key FROM registrations WHERE reg_key = 'K6AA-Y33X-C7ZT-K4TF');
