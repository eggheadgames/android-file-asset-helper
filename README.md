# Android SQLite RO Asset Helper

Copy a versioned SQLite database from APK assets ready to use by SQLite in the app as a read-only database.

This library replaces android-sqlite-asset-helper for use in one specialised scenario: copying a read-only database.

E.g. an app might use a catalogue of data that is updated occaisionally. This data is opened read-only on the device and hence can be updated any time simply by copying over a new database.

Originally I was using https://github.com/jgilfelt/android-sqlite-asset-helper but this is no longer maintained and I've had errors using the various forks. So, for my very specific read-only scenario, I've adapted my similar eggheadgames/android-realm-asset-helper.
