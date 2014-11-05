/***********************************************************************************

 Copyright (C) 2012-2014 Ahmet Öztürk (aoz_2@yahoo.com)

 This file is part of Lifeograph.

 Lifeograph is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Lifeograph is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Lifeograph.  If not, see <http://www.gnu.org/licenses/>.

 ***********************************************************************************/

package net.sourceforge.lifeograph;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


class DialogTags extends Dialog
{
    public DialogTags( Context context, DialogTagsHost host ) {
        super( context );

        mHost = host;

        mAdapterTags = new TagListAdapter( context,
                                           android.R.layout.simple_list_item_multiple_choice,
                                           android.R.id.text1,
                                           mTags,
                                           getLayoutInflater() );
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.dialog_tags );
        setTitle( "Edit Entry Tags" );
        setCancelable( true );
        setOnDismissListener( new android.content.DialogInterface.OnDismissListener() {
            public void onDismiss( android.content.DialogInterface dialog ) {
                invalidateOptionsMenu();
            }
        } );

        ListView listViewTags = ( ListView ) findViewById( R.id.listViewTags );
        listViewTags.setAdapter( mAdapterTags );
        listViewTags.setItemsCanFocus( false );

        buttonAdd = ( Button ) findViewById( R.id.buttonAddTag );
        buttonAdd.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                create_tag();
            }
        } );
        buttonAdd.setEnabled( false );

        editText = ( EditText ) findViewById( R.id.editTextTag );
        editText.addTextChangedListener( new TextWatcher() {
            public void afterTextChanged( Editable s ) {
            }

            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
            }

            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                mFilterText = s.toString();
                update_list();
                if( s.length() > 0 )
                    buttonAdd.setEnabled( Diary.diary.m_tags.get( mFilterText ) == null );
                else
                    buttonAdd.setEnabled( false );
            }
        } );
        editText.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {
                if( v.getText().length() > 0 ) {
                    create_tag();
                    return true;
                }
                return false;
            }
        } );

        update_list();
    }

    @Override
    public void onStop() {
        mHost.onDialogTabsClose();
    }

    private void create_tag() {
        Tag tag = Diary.diary.create_tag( editText.getText().toString(), null );
        mHost.getEntry().add_tag( tag );
        editText.setText( "" );
    }

    private void update_list() {
        mAdapterTags.clear();
        for( Tag t : Diary.diary.m_tags.values() ) {
            if( ! mFilterText.isEmpty() )
                if( !t.get_name().contains( mFilterText ) )
                    continue;
            mTags.add( t );
        }
    }

    // VARIABLES
    protected EditText editText;
    protected String mFilterText = "";
    protected Button buttonAdd;
    private java.util.List< Tag > mTags = new ArrayList< Tag >();
    private TagListAdapter mAdapterTags;

    // TAG LIST ADAPTER CLASS ======================================================================
    class TagListAdapter extends ArrayAdapter< Tag > implements View.OnClickListener
    {
        public TagListAdapter( Context context,
                               int resource,
                               int textViewResourceId,
                               java.util.List< Tag > objects,
                               LayoutInflater inflater ) {
            super( context, resource, textViewResourceId, objects );
            mInflater = inflater;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            ViewHolder holder;
            final Tag tag = getItem( position );

            if( convertView == null ) {
                View view = mInflater.inflate( R.layout.list_item_check, parent, false );
                holder = new ViewHolder( view, DiaryElement.Type.TAG );
                view.setTag( holder );
                convertView = view;
            }
            else {
                holder = ( ViewHolder ) convertView.getTag();
            }

            TextView title = holder.getName();
            title.setText( tag.get_list_str() );

            holder.getIcon().setImageResource( tag.get_icon() );

            Button themeButton = holder.getThemeButton();
            themeButton.setTag( tag );
            themeButton.setOnClickListener( this );
            if( tag.get_has_own_theme() &&
                    mHost.getEntry().m_tags.contains( tag ) ) {
                themeButton.setVisibility( View.VISIBLE );
                if( mHost.getEntry().get_theme_tag() != tag )
                    themeButton.setEnabled( true );
                else
                    themeButton.setEnabled( false );
            }
            else
                themeButton.setVisibility( View.INVISIBLE );

            CheckBox checkBox = holder.getCheckBox();
            checkBox.setChecked( mHost.getEntry().m_tags.contains( tag ) );
            checkBox.setTag( R.id.tag, tag );
            checkBox.setOnClickListener( this );

            if( tag.get_has_own_theme() ) {
                title.setTextColor( tag.get_theme().color_text );
                title.setBackgroundColor( tag.get_theme().color_base );
            }
            else {
                title.setTextColor( Color.BLACK );
                title.setBackgroundColor( Color.argb( 0, 0, 0, 0 ) );
            }

            return convertView;
        }

        public void onClick( View view ) {
            switch( view.getId() ) {
                case R.id.checkBox: {
                    CheckBox cb = ( CheckBox ) view;
                    Tag tag = ( Tag ) cb.getTag( R.id.tag );

                    if( cb.isChecked() ) {
                        mHost.getEntry().add_tag( tag );
                    }
                    else {
                        mHost.getEntry().remove_tag( tag );
                    }

                    DialogTags.this.update_list();
                    break;
                }
                case R.id.buttonTheme: {
                    Button button = ( Button ) view;
                    Tag tag = ( Tag ) button.getTag();

                    if( tag.get_has_own_theme() )
                        mHost.getEntry().set_theme_tag( tag );

                    DialogTags.this.update_list();
                    break;
                }
            }
        }

        private LayoutInflater mInflater;

        // VIEW HOLDER =========================================================================
        private class ViewHolder
        {
            private View mRow;
            private TextView mTitle = null;
            private ImageView mIcon = null;
            private CheckBox mCheckBox = null;
            private Button mThemeButton = null;

            private DiaryElement.Type mType;

            public ViewHolder( View row, DiaryElement.Type type ) {
                mRow = row;
                mType = type;
            }

            public DiaryElement.Type getType() {
                return mType;
            }

            public TextView getName() {
                if( mTitle == null ) {
                    mTitle = ( TextView ) mRow.findViewById( R.id.title );
                }
                return mTitle;
            }

            public ImageView getIcon() {
                if( mIcon == null ) {
                    mIcon = ( ImageView ) mRow.findViewById( R.id.icon );
                }
                return mIcon;
            }

            public CheckBox getCheckBox() {
                if( mCheckBox == null ) {
                    mCheckBox = ( CheckBox ) mRow.findViewById( R.id.checkBox );
                }
                return mCheckBox;
            }

            public Button getThemeButton() {
                if( mThemeButton == null ) {
                    mThemeButton = ( Button ) mRow.findViewById( R.id.buttonTheme );
                }
                return mThemeButton;
            }
        }
    }

    // INTERFACE WITH THE HOST ACTIVITY ============================================================
    public interface DialogTagsHost
    {
        void onDialogTabsClose();
        Entry getEntry();
    }

    protected DialogTagsHost mHost;
}