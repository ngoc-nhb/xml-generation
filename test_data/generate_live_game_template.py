#!/usr/bin/env python3
"""Generate LIVE_GAME template schema and sample input from live_game.xml structure."""

from __future__ import annotations

import json
from pathlib import Path

OUT_DIR = Path(__file__).parent


def element(
    field_name: str,
    parent: str,
    xml_name: str,
    *,
    value_type: str = "STRING",
    empty_handling: str = "OMIT_IF_EMPTY",
    display_order: int,
    source_type: str = "INPUT",
) -> dict:
    return {
        "fieldName": field_name,
        "parentFieldName": parent,
        "xmlName": xml_name,
        "displayName": xml_name,
        "nodeType": "ELEMENT",
        "valueType": value_type,
        "sourceType": source_type,
        "occurrenceRule": None,
        "emptyHandling": empty_handling,
        "requiredWhenParentExists": None,
        "triggerActivation": None,
        "defaultValue": None,
        "staticValue": None,
        "xmlPath": None,
        "namespace": None,
        "displayOrder": display_order,
        "description": None,
    }


def attribute(
    field_name: str,
    parent: str,
    xml_name: str,
    *,
    display_order: int,
    value_type: str = "STRING",
) -> dict:
    return {
        "fieldName": field_name,
        "parentFieldName": parent,
        "xmlName": xml_name,
        "displayName": xml_name,
        "nodeType": "ATTRIBUTE",
        "valueType": value_type,
        "sourceType": "INPUT",
        "occurrenceRule": None,
        "emptyHandling": "REQUIRED",
        "requiredWhenParentExists": None,
        "triggerActivation": None,
        "defaultValue": None,
        "staticValue": None,
        "xmlPath": None,
        "namespace": None,
        "displayOrder": display_order,
        "description": None,
    }


def group(
    field_name: str,
    parent: str | None,
    xml_name: str,
    *,
    display_order: int,
    occurrence: str | None = None,
) -> dict:
    return {
        "fieldName": field_name,
        "parentFieldName": parent,
        "xmlName": xml_name,
        "displayName": xml_name,
        "nodeType": "GROUP",
        "valueType": None,
        "sourceType": None,
        "occurrenceRule": occurrence,
        "emptyHandling": "REQUIRED",
        "requiredWhenParentExists": None,
        "triggerActivation": None,
        "defaultValue": None,
        "staticValue": None,
        "xmlPath": None,
        "namespace": None,
        "displayOrder": display_order,
        "description": None,
    }


def build_fields() -> list[dict]:
    fields: list[dict] = []
    fields.append(group("Football", None, "Football", display_order=1, occurrence="ONE_OR_MORE"))
    fields.append(group("GameReport", "Football", "GameReport", display_order=1, occurrence="ONE_OR_MORE"))

    game_report_scalars = [
        ("GameID", "STRING", "REQUIRED"),
        ("GameDate", "STRING", "REQUIRED"),
        ("LocalDate", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("GameKindID", "INTEGER", "REQUIRED"),
        ("GameKindName", "STRING", "REQUIRED"),
        ("SeasonID", "INTEGER", "REQUIRED"),
        ("SeasonName", "STRING", "REQUIRED"),
        ("StartTime", "STRING", "REQUIRED"),
        ("StadiumID", "INTEGER", "REQUIRED"),
        ("StadiumName", "STRING", "REQUIRED"),
        ("DayNight", "INTEGER", "REQUIRED"),
        ("StateID", "INTEGER", "REQUIRED"),
        ("StateName", "STRING", "REQUIRED"),
        ("Time", "INTEGER", "REQUIRED"),
        ("Half", "INTEGER", "REQUIRED"),
        ("SituationID", "INTEGER", "REQUIRED"),
        ("SituationName", "STRING", "REQUIRED"),
        ("RefereeID", "INTEGER", "REQUIRED"),
        ("Referee", "STRING", "REQUIRED"),
        ("Linesman1", "STRING", "REQUIRED"),
        ("Linesman2", "STRING", "REQUIRED"),
        ("Fourth", "STRING", "REQUIRED"),
        ("OtherReferee1", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("OtherReferee2", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("VAR", "STRING", "REQUIRED"),
        ("AVAR1", "STRING", "REQUIRED"),
        ("AVAR2", "STRING", "REQUIRED"),
        ("AVAR3", "STRING", "REQUIRED"),
        ("HalfEndF", "INTEGER", "REQUIRED"),
        ("GameFixF", "INTEGER", "REQUIRED"),
        ("GameTime", "STRING", "REQUIRED"),
        ("APT", "STRING", "REQUIRED"),
    ]
    for idx, (name, vtype, empty) in enumerate(game_report_scalars, start=1):
        fields.append(
            element(name, "GameReport", name, value_type=vtype, empty_handling=empty, display_order=idx)
        )

    fields.append(group("TeamInfo", "GameReport", "TeamInfo", display_order=40, occurrence="ZERO_OR_MORE"))
    fields.append(attribute("TeamInfo_HV", "TeamInfo", "HV", display_order=1))

    team_info_scalars = [
        ("TeamInfo_ID", "ID", "STRING", "REQUIRED"),
        ("TeamInfo_NameS", "NameS", "STRING", "REQUIRED"),
        ("TeamInfo_DirectorID", "DirectorID", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_Director", "Director", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_PostName", "PostName", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_BeforePoint", "BeforePoint", "INTEGER", "REQUIRED"),
        ("TeamInfo_AfterPoint", "AfterPoint", "INTEGER", "REQUIRED"),
        ("TeamInfo_FormationID", "FormationID", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_FormationName", "FormationName", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_Score", "Score", "INTEGER", "REQUIRED"),
        ("TeamInfo_PKBScore", "PKBScore", "INTEGER", "REQUIRED"),
        ("TeamInfo_PKScore", "PKScore", "INTEGER", "REQUIRED"),
        ("TeamInfo_PK", "PK", "INTEGER", "REQUIRED"),
        ("TeamInfo_GK", "GK", "INTEGER", "REQUIRED"),
        ("TeamInfo_Shoot", "Shoot", "INTEGER", "REQUIRED"),
        ("TeamInfo_Assist", "Assist", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_FKD", "FKD", "INTEGER", "REQUIRED"),
        ("TeamInfo_FKI", "FKI", "INTEGER", "REQUIRED"),
        ("TeamInfo_CK", "CK", "INTEGER", "REQUIRED"),
        ("TeamInfo_Yellow", "Yellow", "INTEGER", "REQUIRED"),
        ("TeamInfo_Red", "Red", "INTEGER", "REQUIRED"),
        ("TeamInfo_ShootGoal", "ShootGoal", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("TeamInfo_Offside", "Offside", "INTEGER", "REQUIRED"),
        ("TeamInfo_Gain", "Gain", "STRING", "EMPTY_TAG_IF_EMPTY"),
    ]
    for idx, (fname, xml, vtype, empty) in enumerate(team_info_scalars, start=2):
        fields.append(element(fname, "TeamInfo", xml, value_type=vtype, empty_handling=empty, display_order=idx))

    fields.append(group("GameState", "TeamInfo", "GameState", display_order=30, occurrence="ZERO_OR_MORE"))
    fields.append(attribute("GameState_ID", "GameState", "ID", display_order=1))
    game_state_scalars = [
        ("GameState_Score", "Score", "INTEGER"),
        ("GameState_PK", "PK", "INTEGER"),
        ("GameState_GK", "GK", "INTEGER"),
        ("GameState_Shoot", "Shoot", "INTEGER"),
        ("GameState_FK", "FK", "INTEGER"),
        ("GameState_Yellow", "Yellow", "INTEGER"),
        ("GameState_Red", "RED", "INTEGER"),
        ("GameState_CK", "CK", "INTEGER"),
        ("GameState_ShootGoal", "ShootGoal", "STRING"),
        ("GameState_Offside", "Offside", "INTEGER"),
        ("GameState_Gain", "Gain", "STRING"),
    ]
    # fix Red xml name
    game_state_scalars[6] = ("GameState_Red", "Red", "INTEGER")
    for idx, (fname, xml, vtype) in enumerate(game_state_scalars, start=2):
        fields.append(element(fname, "GameState", xml, value_type=vtype, empty_handling="REQUIRED", display_order=idx))

    fields.append(group("PKInfo", "GameReport", "PKInfo", display_order=50, occurrence="ZERO_OR_MORE"))
    fields.append(attribute("PKInfo_HV", "PKInfo", "HV", display_order=1))
    fields.append(group("Player", "PKInfo", "Player", display_order=2, occurrence="ZERO_OR_MORE"))
    player_scalars = [
        ("Player_No", "No", "INTEGER"),
        ("Player_SerialNo", "SerialNo", "INTEGER"),
        ("Player_PlayerID", "PlayerID", "INTEGER"),
        ("Player_PlayerName", "PlayerName", "STRING"),
        ("Player_PlayerNameS", "PlayerNameS", "STRING"),
        ("Player_PlayerUni", "PlayerUni", "INTEGER"),
        ("Player_SuccessF", "SuccessF", "INTEGER"),
        ("Player_Sign", "Sign", "STRING"),
    ]
    for idx, (fname, xml, vtype) in enumerate(player_scalars, start=1):
        fields.append(element(fname, "Player", xml, value_type=vtype, empty_handling="REQUIRED", display_order=idx))

    fields.append(group("GoalInfo", "GameReport", "GoalInfo", display_order=60, occurrence="ZERO_OR_MORE"))
    fields.append(attribute("GoalInfo_No", "GoalInfo", "No", display_order=1))
    goal_scalars = [
        ("GoalInfo_StateID", "StateID", "INTEGER"),
        ("GoalInfo_StateName", "StateName", "STRING"),
        ("GoalInfo_Time", "Time", "INTEGER"),
        ("GoalInfo_Half", "Half", "INTEGER"),
        ("GoalInfo_TeamID", "TeamID", "STRING"),
        ("GoalInfo_TeamNameS", "TeamNameS", "STRING"),
        ("GoalInfo_GPlayerID", "GPlayerID", "INTEGER"),
        ("GoalInfo_GPlayerName", "GPlayerName", "STRING"),
        ("GoalInfo_GPlayerNameS", "GPlayerNameS", "STRING"),
        ("GoalInfo_GPlayerUni", "GPlayerUni", "INTEGER"),
        ("GoalInfo_Body", "Body", "STRING"),
        ("GoalInfo_Operation", "Operation", "STRING"),
        ("GoalInfo_APlayerID", "APlayerID", "INTEGER"),
        ("GoalInfo_APlayerName", "APlayerName", "STRING"),
        ("GoalInfo_APlayerNameS", "APlayerNameS", "STRING"),
        ("GoalInfo_APlayerUni", "APlayerUni", "INTEGER"),
        ("GoalInfo_AssistKind", "AssistKind", "STRING", "EMPTY_TAG_IF_EMPTY"),
        ("GoalInfo_PKGoalF", "PKGoalF", "INTEGER"),
        ("GoalInfo_OwnGoalF", "OwnGoalF", "INTEGER"),
        ("GoalInfo_ScoreTeamID", "ScoreTeamID", "STRING"),
        ("GoalInfo_ScoreTeamNameS", "ScoreTeamNameS", "STRING"),
    ]
    for idx, item in enumerate(goal_scalars, start=2):
        if len(item) == 4:
            fname, xml, vtype, empty = item
        else:
            fname, xml, vtype = item
            empty = "REQUIRED"
        fields.append(element(fname, "GoalInfo", xml, value_type=vtype, empty_handling=empty, display_order=idx))

    fields.append(group("GameEndInfo", "GameReport", "GameEndInfo", display_order=70, occurrence="ONE_OR_MORE"))
    end_scalars = [
        ("GameEndInfo_Condition", "Condition", "STRING"),
        ("GameEndInfo_Surface", "Surface", "STRING"),
        ("GameEndInfo_Weather", "Weather", "STRING"),
        ("GameEndInfo_Wind", "Wind", "STRING"),
        ("GameEndInfo_Temperature", "Temperature", "DECIMAL"),
        ("GameEndInfo_Spectators", "Spectators", "LONG"),
        ("GameEndInfo_Humidity", "Humidity", "INTEGER"),
    ]
    for idx, (fname, xml, vtype) in enumerate(end_scalars, start=1):
        fields.append(element(fname, "GameEndInfo", xml, value_type=vtype, empty_handling="REQUIRED", display_order=idx))

    # display_order must be unique per template on initial insert (parent_id is set in a second pass).
    for order, field in enumerate(fields, start=1):
        field["displayOrder"] = order

    names = [f["fieldName"] for f in fields]
    assert len(names) == len(set(names)), "duplicate fieldName detected"
    return fields


def build_input() -> dict:
    """Sample inputData matching live_game.xml (fieldName keys, nested by group)."""
    game_end_info = {
        "GameEndInfo_Condition": "全面良芝",
        "GameEndInfo_Surface": "乾燥",
        "GameEndInfo_Weather": "晴",
        "GameEndInfo_Wind": "弱風",
        "GameEndInfo_Temperature": 29.4,
        "GameEndInfo_Spectators": 881234,
        "GameEndInfo_Humidity": 68,
    }

    game_report = {
        "GameID": "2026062339",
        "GameDate": "20260623",
        "LocalDate": "",
        "GameKindID": 68,
        "GameKindName": "明治安田Ｊ３リーグ",
        "SeasonID": 1,
        "SeasonName": "シーズン（通算）",
        "StartTime": "1400",
        "StadiumID": 30317,
        "StadiumName": "相模原ギオンスタジアム",
        "DayNight": 1,
        "StateID": 7,
        "StateName": "PK戦",
        "Time": 90,
        "Half": 45,
        "SituationID": 2,
        "SituationName": "試合中",
        "RefereeID": 1400420,
        "Referee": "上原　直人",
        "Linesman1": "山口　大輔",
        "Linesman2": "宮原　一也",
        "Fourth": "原田　大輔",
        "OtherReferee1": "",
        "OtherReferee2": "",
        "VAR": "吉田 哲朗",
        "AVAR1": "西橋 勲",
        "AVAR2": "大輔 橋勲",
        "AVAR3": "宮原 大也",
        "HalfEndF": 1,
        "GameFixF": 1,
        "GameTime": "110:54",
        "APT": "90:00",
        "TeamInfo": [
            {
                "TeamInfo_HV": "1",
                "TeamInfo_ID": " 30676 ",
                "TeamInfo_NameS": "相模原",
                "TeamInfo_DirectorID": "",
                "TeamInfo_Director": "",
                "TeamInfo_PostName": "",
                "TeamInfo_BeforePoint": 0,
                "TeamInfo_AfterPoint": 0,
                "TeamInfo_FormationID": "",
                "TeamInfo_FormationName": "",
                "TeamInfo_Score": 1,
                "TeamInfo_PKBScore": 2,
                "TeamInfo_PKScore": 2,
                "TeamInfo_PK": 2,
                "TeamInfo_GK": 0,
                "TeamInfo_Shoot": 0,
                "TeamInfo_Assist": "",
                "TeamInfo_FKD": 0,
                "TeamInfo_FKI": 0,
                "TeamInfo_CK": 0,
                "TeamInfo_Yellow": 0,
                "TeamInfo_Red": 0,
                "TeamInfo_ShootGoal": "",
                "TeamInfo_Offside": 0,
                "TeamInfo_Gain": "",
                "GameState": [
                    {
                        "GameState_ID": "1",
                        "GameState_Score": 0,
                        "GameState_PK": 0,
                        "GameState_GK": 0,
                        "GameState_Shoot": 0,
                        "GameState_FK": 0,
                        "GameState_Yellow": 0,
                        "GameState_Red": 0,
                        "GameState_CK": 0,
                        "GameState_ShootGoal": "0",
                        "GameState_Offside": 0,
                        "GameState_Gain": "0",
                    }
                ],
            },
            {
                "TeamInfo_HV": "2",
                "TeamInfo_ID": " 30305 ",
                "TeamInfo_NameS": "栃木SC",
                "TeamInfo_DirectorID": "",
                "TeamInfo_Director": "",
                "TeamInfo_PostName": "",
                "TeamInfo_BeforePoint": 0,
                "TeamInfo_AfterPoint": 0,
                "TeamInfo_FormationID": "",
                "TeamInfo_FormationName": "",
                "TeamInfo_Score": 1,
                "TeamInfo_PKBScore": 3,
                "TeamInfo_PKScore": 3,
                "TeamInfo_PK": 3,
                "TeamInfo_GK": 0,
                "TeamInfo_Shoot": 4,
                "TeamInfo_Assist": "",
                "TeamInfo_FKD": 0,
                "TeamInfo_FKI": 0,
                "TeamInfo_CK": 0,
                "TeamInfo_Yellow": 0,
                "TeamInfo_Red": 0,
                "TeamInfo_ShootGoal": "",
                "TeamInfo_Offside": 0,
                "TeamInfo_Gain": "0",
                "GameState": [
                    {
                        "GameState_ID": "1",
                        "GameState_Score": 0,
                        "GameState_PK": 0,
                        "GameState_GK": 0,
                        "GameState_Shoot": 0,
                        "GameState_FK": 0,
                        "GameState_Yellow": 0,
                        "GameState_Red": 0,
                        "GameState_CK": 0,
                        "GameState_ShootGoal": "0",
                        "GameState_Offside": 0,
                        "GameState_Gain": "0",
                    }
                ],
            },
        ],
        "PKInfo": [
            {
                "PKInfo_HV": "1",
                "Player": [
                    {
                        "Player_No": 1,
                        "Player_SerialNo": 1,
                        "Player_PlayerID": 1200059,
                        "Player_PlayerName": "白崎 凌兵",
                        "Player_PlayerNameS": "白崎",
                        "Player_PlayerUni": 41,
                        "Player_SuccessF": 1,
                        "Player_Sign": "○",
                    },
                    {
                        "Player_No": 2,
                        "Player_SerialNo": 3,
                        "Player_PlayerID": 1200049,
                        "Player_PlayerName": "後藤 優介",
                        "Player_PlayerNameS": "後藤",
                        "Player_PlayerUni": 14,
                        "Player_SuccessF": 0,
                        "Player_Sign": "×",
                    },
                    {
                        "Player_No": 3,
                        "Player_SerialNo": 5,
                        "Player_PlayerID": 700799,
                        "Player_PlayerName": "乾 貴士",
                        "Player_PlayerNameS": "乾",
                        "Player_PlayerUni": 33,
                        "Player_SuccessF": 0,
                        "Player_Sign": "×",
                    },
                    {
                        "Player_No": 4,
                        "Player_SerialNo": 7,
                        "Player_PlayerID": 1200349,
                        "Player_PlayerName": "井林 章",
                        "Player_PlayerNameS": "井林",
                        "Player_PlayerUni": 38,
                        "Player_SuccessF": 0,
                        "Player_Sign": "×",
                    },
                    {
                        "Player_No": 5,
                        "Player_SerialNo": 9,
                        "Player_PlayerID": 1635021,
                        "Player_PlayerName": "菊地 脩太",
                        "Player_PlayerNameS": "菊地",
                        "Player_PlayerUni": 83,
                        "Player_SuccessF": 1,
                        "Player_Sign": "○",
                    },
                ],
            },
            {
                "PKInfo_HV": "2",
                "Player": [
                    {
                        "Player_No": 1,
                        "Player_SerialNo": 2,
                        "Player_PlayerID": 1600146,
                        "Player_PlayerName": "瀬川 祐輔",
                        "Player_PlayerNameS": "瀬川",
                        "Player_PlayerUni": 20,
                        "Player_SuccessF": 1,
                        "Player_Sign": "○",
                    },
                    {
                        "Player_No": 2,
                        "Player_SerialNo": 4,
                        "Player_PlayerID": 1601436,
                        "Player_PlayerName": "小西 雄大",
                        "Player_PlayerNameS": "小西",
                        "Player_PlayerUni": 21,
                        "Player_SuccessF": 0,
                        "Player_Sign": "×",
                    },
                    {
                        "Player_No": 3,
                        "Player_SerialNo": 6,
                        "Player_PlayerID": 1600210,
                        "Player_PlayerName": "三丸 拡",
                        "Player_PlayerNameS": "三丸",
                        "Player_PlayerUni": 2,
                        "Player_SuccessF": 0,
                        "Player_Sign": "×",
                    },
                    {
                        "Player_No": 4,
                        "Player_SerialNo": 8,
                        "Player_PlayerID": 1611125,
                        "Player_PlayerName": "戸嶋 祥郎",
                        "Player_PlayerNameS": "戸嶋",
                        "Player_PlayerUni": 28,
                        "Player_SuccessF": 0,
                        "Player_Sign": "○",
                    },
                    {
                        "Player_No": 5,
                        "Player_SerialNo": 10,
                        "Player_PlayerID": 1617910,
                        "Player_PlayerName": "渡井 理己",
                        "Player_PlayerNameS": "渡井",
                        "Player_PlayerUni": 11,
                        "Player_SuccessF": 1,
                        "Player_Sign": "○",
                    },
                ],
            },
        ],
        "GoalInfo": [
            {
                "GoalInfo_No": "1",
                "GoalInfo_StateID": 1,
                "GoalInfo_StateName": "前半",
                "GoalInfo_Time": 15,
                "GoalInfo_Half": 15,
                "GoalInfo_TeamID": " 30676 ",
                "GoalInfo_TeamNameS": "相模原",
                "GoalInfo_GPlayerID": 1651283,
                "GoalInfo_GPlayerName": "ラファエル フルタード",
                "GoalInfo_GPlayerNameS": "Ｒフルタード",
                "GoalInfo_GPlayerUni": 9,
                "GoalInfo_Body": "右",
                "GoalInfo_Operation": "クロス",
                "GoalInfo_APlayerID": 1632479,
                "GoalInfo_APlayerName": "加藤 拓己",
                "GoalInfo_APlayerNameS": "加藤拓",
                "GoalInfo_APlayerUni": 23,
                "GoalInfo_AssistKind": "",
                "GoalInfo_PKGoalF": 0,
                "GoalInfo_OwnGoalF": 0,
                "GoalInfo_ScoreTeamID": " 30676 ",
                "GoalInfo_ScoreTeamNameS": "相模原",
            },
            {
                "GoalInfo_No": "2",
                "GoalInfo_StateID": 2,
                "GoalInfo_StateName": "後半",
                "GoalInfo_Time": 65,
                "GoalInfo_Half": 20,
                "GoalInfo_TeamID": " 30676 ",
                "GoalInfo_TeamNameS": "相模原",
                "GoalInfo_GPlayerID": 1651283,
                "GoalInfo_GPlayerName": "ラファエル フルタード",
                "GoalInfo_GPlayerNameS": "Ｒフルタード",
                "GoalInfo_GPlayerUni": 9,
                "GoalInfo_Body": "右",
                "GoalInfo_Operation": "クロス",
                "GoalInfo_APlayerID": 1632479,
                "GoalInfo_APlayerName": "加藤 拓己",
                "GoalInfo_APlayerNameS": "加藤拓",
                "GoalInfo_APlayerUni": 23,
                "GoalInfo_AssistKind": "",
                "GoalInfo_PKGoalF": 0,
                "GoalInfo_OwnGoalF": 1,
                "GoalInfo_ScoreTeamID": " 30305 ",
                "GoalInfo_ScoreTeamNameS": "栃木SC",
            },
        ],
        "GameEndInfo": game_end_info,
    }

    return {"GameReport": game_report}


def main() -> None:
    fields = build_fields()
    template_payload = {
        "code": "LIVE_GAME",
        "name": "Live Game Report",
        "description": "Template matching test_data/live_game.xml for MVP testing",
        "schema": {"fields": fields, "mappings": []},
    }
    preview_payload = {
        "inputData": build_input(),
        "selectedMasterData": {},
    }

    (OUT_DIR / "live_game_create_template.json").write_text(
        json.dumps(template_payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (OUT_DIR / "live_game_input.json").write_text(
        json.dumps(preview_payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Wrote {len(fields)} fields to live_game_create_template.json")
    print("Wrote live_game_input.json")


if __name__ == "__main__":
    main()
