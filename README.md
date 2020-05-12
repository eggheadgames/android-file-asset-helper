# Android SQLite RO Asset Helper

Copy a versioned SQLite database from APK assets ready to use by SQLite in the app as a read-only database.

This library replaces android-sqlite-asset-helper for use in one specialised scenario: copying a read-only database.

E.g. an app might use a catalogue of data that is updated occaisionally. This data is opened read-only on the device and hence can be updated any time simply by copying over a new database.

## Example

 * create an asset `mydatabase_1.db` in your Android assets
 * call this helper on app startup
 
If the numeric extension on the database has changed, then the `mydatabase_1.db` file will be copied and renamed to `mydatabase.db`. Any existing `mydatabase.db` will be replaced. 

Later, when there is a new version of the catalogue data, the `mydatabase_1.db` file is replaced in the assets with `mydatabase_2.db`. 

## Installation Instructions

Add the JitPack.io repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add a dependency to your application related `build.gradle`

```gradle
dependencies {
    compile 'com.github.eggheadgames:android-sqlite-ro-asset--helper:2.0.0'
}
```

## Alternatives

Originally I was using https://github.com/jgilfelt/android-sqlite-asset-helper but this is no longer maintained and I've had errors using the various forks with recent Android versions. So, for my very specific read-only scenario, I've adapted my similar eggheadgames/android-realm-asset-helper.

Android Room provides a mechanism for this. Here is a guide: https://medium.com/androiddevelopers/packing-the-room-pre-populate-your-database-with-this-one-method-333ae190e680

## Explanation

It can be convenient to have a read-only database as part of an apk. This might have game level information, for example, or other data like zip codes or product information. If the app treats it as "read-only" (perhaps also using a separate realm database for storing other state data), then data updates are as simple as updating the "master" realm file in the apk and then copying it over on first run after the user updates.

iOS note: This is conceptually simpler in iOS, because database engines can access read-only data directly from the application bundle (= apk). This library was originally created so that our iOS and Android apps could share the same read-only SQLite database.

This helper library adds support for this "read-only data included in apk" scenario.

For efficiency, the copy should only be made when the database has changed. This is handled as follows:

 * if no copy of the database exists, it is copied
 * if a copy exists, then a sharedPreference value is checked to see what database version it is (defaults to `0` if not found)
 * the APK `assets` folder is searched for the database name with a postfix `_NN` in the name (e.g. `products_12`). If the `NN` value is higher than the current version, then the new database is copied (with the `_NN` removed) and the sharedPreference value is updated
 * if no database is found in assets, this causes an immediate error (as this is usually an oversight and should be resolved ASAP)

Thus, the workflow for an apk with read-only data becomes:

 * store the database, e.g. `products` in `assets` with the name `products_0`
 * when products is updated, rename the fie from `products_0` to `products_1`

The helper will see the change and copy the database as needed.

## Caveats

There is no consideration given (so far) to database migration requirements or any sort of "update my user's existing SQLite database from this new SQLite database". That is clearly a useful enhancement to think about for the future but was beyond the scope of the initial release.

## Pull Requests, Issues, Feedback

We welcome pull requests. If you have questions or bugs, please open an issue and we'll respond promptly.
