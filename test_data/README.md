# Live Game test data

Sample XML and matching template metadata derived from `live_game.xml`.

## Files

| File | Purpose |
| ---- | ------- |
| `live_game.xml` | Reference XML output (J-League live game report) |
| `live_game_create_template.json` | `POST /api/v1/templates` body with full schema (**115 fields**) |
| `live_game_input.json` | Preview/export body (`inputData` + `selectedMasterData`) |
| `generate_live_game_template.py` | Regenerates JSON from the XML structure |

## Template structure

```text
Football
└── GameReport
    ├── GameID, GameDate, … (32 scalar elements)
    ├── TeamInfo[] (attribute HV + team stats + GameState[])
    ├── PKInfo[] (attribute HV + Player[])
    ├── GoalInfo[] (attribute No + goal details)
    └── GameEndInfo (match conditions)
```

- **fieldName** is globally unique (e.g. `TeamInfo_Score`, `GameState_Score`).
- **xmlName** matches the original XML element/attribute name.
- Repeating groups use `occurrenceRule: ZERO_OR_MORE`; provide arrays in input JSON.
- All value fields use `sourceType: INPUT` (no master-data mappings required for basic testing).

## Create template (API)

```bash
curl -s -X POST http://localhost:8080/api/v1/templates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @test_data/live_game_create_template.json
```

If code `LIVE_GAME` already exists, delete the old template first or change `"code"` in the JSON.

## Preview

Replace `{id}` with the template id from create response:

```bash
curl -s -X POST "http://localhost:8080/api/v1/templates/{id}/preview" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @test_data/live_game_input.json
```

## UI (XML Generation)

1. Create template via **Templates → New** (or import via API above).
2. Open **Schema editor** — paste fields from `live_game_create_template.json` → `schema.fields` if not created via API.
3. Open **XML Generation**, select **LIVE_GAME**.
4. Paste contents of `live_game_input.json` → `inputData` into the JSON editor (the nested `GameReport` object only, or full file’s `inputData` key).
5. **Preview** — expect XML similar to `live_game.xml`.

## Regenerate

After editing the generator:

```bash
python3 test_data/generate_live_game_template.py
```
