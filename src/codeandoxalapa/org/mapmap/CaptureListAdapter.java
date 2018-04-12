package codeandoxalapa.org.mapmap;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CaptureListAdapter extends ArrayAdapter<RouteCapture> {
    private ArrayList<RouteCapture> routes;

    
    private int listItemLayoutResId;
    
    public CaptureListAdapter(Context context, ArrayList<RouteCapture> routes) {
        super(context, R.layout.capture_list_item_layout, routes);
        this.routes = routes;
        listItemLayoutResId = R.layout.capture_list_item_layout;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	
        View v = convertView;
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            v = vi.inflate(listItemLayoutResId, parent, false);
        }
            
        RouteCapture route = routes.get(position);
        
        if (route != null) {
        	
            TextView routeName = (TextView) v.findViewById(R.id.text1);
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue_bold.ttf");
            routeName.setTypeface(font);
    		
            TextView details = (TextView) v.findViewById(R.id.text2);
            Typeface font1 = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue_bold.ttf");
            details.setTypeface(font1);
    		

            routeName.setText(route.name);
            details.setText("Paradas: " + route.stops.size() + " Puntos: " + route.points.size());
            
            ImageButton editBtn = (ImageButton) v.findViewById(R.id.edit_btn);
            ImageButton mapBtn = (ImageButton) v.findViewById(R.id.map_btn);
            
            editBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) { 
                    //do something
                	Log.v("editBtn", "click");
                	EditActivity.itemPosition = position;
                	Intent settingsIntent = new Intent(v.getContext(), EditActivity.class);
      			  	v.getContext().startActivity(settingsIntent);
                }
            });
            
            mapBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) { 
                    //do something
                	Log.v("MapBtn", "click");
                	MapActivity.itemPosition = position;
                	Intent mapIntent = new Intent(v.getContext(), MapActivity.class);
      			  	v.getContext().startActivity(mapIntent);
                }
            });
        }
        return v;
    }
}