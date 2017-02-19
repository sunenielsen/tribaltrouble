USE oddlabs;

DELETE game_reports FROM game_reports JOIN games ON game_reports.game_id = games.id WHERE games.status IN ('completed', 'dropped');
