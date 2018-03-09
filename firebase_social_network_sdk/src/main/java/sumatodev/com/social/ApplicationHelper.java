/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package sumatodev.com.social;

import sumatodev.com.social.listeners.LoginRequiredListener;
import sumatodev.com.social.managers.DatabaseHelper;

/**
 * Created by Kristina on 10/28/16.
 */

public class ApplicationHelper {

    private static final String TAG = ApplicationHelper.class.getSimpleName();
    private static DatabaseHelper databaseHelper;
    public static String storageBucketLink;
    public static LoginRequiredListener mLoginRequiredListener;

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public static void initDatabaseHelper(android.app.Application application, String storageLink, LoginRequiredListener loginRequiredListener) {
        databaseHelper = DatabaseHelper.getInstance(application);
        databaseHelper.init();
        storageBucketLink = storageLink;
        mLoginRequiredListener = loginRequiredListener;
    }
}
