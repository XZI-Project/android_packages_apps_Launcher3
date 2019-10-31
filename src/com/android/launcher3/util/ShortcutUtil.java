/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.util;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

import android.content.Context;
import android.content.pm.ShortcutInfo;

import androidx.annotation.NonNull;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.Utilities;
import com.android.launcher3.WorkspaceItemInfo;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.icons.LauncherIcons;
import com.android.launcher3.model.WidgetsModel;
import com.android.launcher3.shortcuts.ShortcutKey;

public class ShortcutUtil {
    /**
     * Returns true when we should show shortcut menu for the item.
     */
    public static boolean supportsShortcuts(ItemInfo info) {
        return isActive(info) && (isApp(info) || isPinnedShortcut(info));
    }

    /**
     * Returns true when we should show depp shortcuts in shortcut menu for the item.
     */
    public static boolean supportsDeepShortcuts(ItemInfo info) {
        return isActive(info) && isApp(info);
    }

    /**
     * Returns the shortcut id if the item is a pinned shortcut.
     */
    public static String getShortcutIdIfPinnedShortcut(ItemInfo info) {
        return isActive(info) && isPinnedShortcut(info)
                ? ShortcutKey.fromItemInfo(info).getId() : null;
    }

    /**
     * Returns the person keys associated with the item. (Has no function right now.)
     */
    public static String[] getPersonKeysIfPinnedShortcut(ItemInfo info) {
        return isActive(info) && isPinnedShortcut(info)
                ? ((WorkspaceItemInfo) info).getPersonKeys() : Utilities.EMPTY_STRING_ARRAY;
    }

    /**
     * Returns true if the item is a deep shortcut.
     */
    public static boolean isDeepShortcut(ItemInfo info) {
        return info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
                && info instanceof WorkspaceItemInfo;
    }

    /**
     * Fetch the shortcut icon in background, then update the UI.
     */
    public static void fetchAndUpdateShortcutIconAsync(
            @NonNull Context context, @NonNull WorkspaceItemInfo info, @NonNull ShortcutInfo si,
            boolean badged) {
        if (info.iconBitmap == null) {
            // use low res icon as placeholder while the actual icon is being fetched.
            info.iconBitmap = BitmapInfo.LOW_RES_ICON;
            info.iconColor = Themes.getColorAccent(context);
        }
        MODEL_EXECUTOR.execute(() -> {
            LauncherIcons li = LauncherIcons.obtain(context);
            BitmapInfo bitmapInfo = li.createShortcutIcon(si, badged, true, null);
            info.applyFrom(bitmapInfo);
            li.recycle();
            LauncherAppState.getInstance(context).getModel().updateAndBindWorkspaceItem(info, si);
        });
    }

    private static boolean isActive(ItemInfo info) {
        boolean isLoading = info instanceof WorkspaceItemInfo
                && ((WorkspaceItemInfo) info).hasPromiseIconUi();
        return !isLoading && !info.isDisabled() && !WidgetsModel.GO_DISABLE_WIDGETS;
    }

    private static boolean isApp(ItemInfo info) {
        return info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    }

    private static boolean isPinnedShortcut(ItemInfo info) {
        return info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
                && info.container != ItemInfo.NO_ID
                && info instanceof WorkspaceItemInfo;
    }
}