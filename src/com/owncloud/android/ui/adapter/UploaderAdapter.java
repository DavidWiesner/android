/**
 *   ownCloud Android client application
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2016 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.datamodel.ThumbnailLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploaderAdapter extends SimpleAdapter {

    private final ThumbnailLoader thumbLoader;
    private Context mContext;
    private LayoutInflater inflater;

    public UploaderAdapter(Context context,
                           List<? extends Map<String, ?>> data, int resource, String[] from,
                           int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        thumbLoader = new ThumbnailLoader(context, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.uploader_list_item_layout, null);

        HashMap<String, OCFile> data = (HashMap<String, OCFile>) getItem(position);
        OCFile file = data.get("dirname");

        TextView filename = (TextView) vi.findViewById(R.id.filename);
        filename.setText(file.getFileName());
        
        ImageView fileIcon = (ImageView) vi.findViewById(R.id.thumbnail);
        thumbLoader.load(file).into(fileIcon);

        // TODO enable after #1277 is merged
//        TextView lastModV = (TextView) vi.findViewById(R.id.last_mod);
//        lastModV.setText(DisplayUtils.getRelativeTimestamp(mContext, file));

        return vi;
    }


}
