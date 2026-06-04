# RFIDSearch

A minimal one-button Android app that connects to a Zebra RFID handheld scanner and performs an inventory scan.

## How it works

1. App launches → automatically connects to the first available RFID reader
2. Press the **Search** button → calls `reader.Actions.Inventory.perform()`
3. Reader scans all RFID tags in range → SDK fires `eventReadNotify()` callback
4. Tag EPCs are collected into a thread-safe `Set` (duplicates removed automatically)
5. Results displayed on screen as `Found X tag(s): [EPC list]`

## Note on "Search"

I interpreted "Search" as triggering an Inventory scan using `reader.Actions.Inventory.perform()`, which scans all RFID tags in range.

If the intent was to locate a **specific** tag by EPC, that would use `reader.Actions.TagLocationing.perform(tagID)` (https://techdocs.zebra.com/dcs/rfid/android/2-0-5-275/apis/com/zebra/rfid/api3/taglocationing) instead, which continuously scans and returns a relative distance (0–100) indicating proximity to the target tag.

## SDK

Built with **Zebra RFID API3 SDK v2.0.5.275**

- Library used: `rfidapi3lib-2.0.5.275.aar` (Handheld transport)
- Key API: `com.zebra.rfid.api3.Inventory` (https://techdocs.zebra.com/dcs/rfid/android/2-0-5-275/apis/com/zebra/rfid/api3/inventory)
- Documentation: [techdocs.zebra.com/dcs/rfid/android/2-0-5-275](https://techdocs.zebra.com/dcs/rfid/android/2-0-5-275/)

## Requirements

- Android API 29+
- Zebra handheld device with RFID reader (e.g. RFD40, MC3330R)
