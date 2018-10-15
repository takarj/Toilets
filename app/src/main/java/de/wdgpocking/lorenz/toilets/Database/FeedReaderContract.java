package de.wdgpocking.lorenz.toilets.Database;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "toilets";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSITION_LAT = "pos_lat";
        public static final String COLUMN_NAME_POSITION_LNG = "pos_lng";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_PRICE = "price";
    }
}