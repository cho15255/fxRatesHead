package com.example.ratesheads;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class InstrumentList extends ListFragment {
	  String[] numbers_text = new String[] {};   
	  List<String> instrumentList;
	  @Override  
	  public void onListItemClick(ListView l, View v, int position, long id) {  
		  v.setSelected(true);
		  MainActivity.setHeadInstrument(instrumentList.get(position));
		  
	  }  
	  
	  LayoutInflater layoutInflater;
	  
	  @Override  
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,  
	    Bundle savedInstanceState) {
		  this.layoutInflater = inflater;
	   ArrayAdapter<String> adapter = new ArrayAdapter<String>(  
	     inflater.getContext(), android.R.layout.simple_list_item_1,  
	     numbers_text);
	   setListAdapter(adapter);
	   return super.onCreateView(inflater, container, savedInstanceState);  
	  }    
	
	public void setInstruments(List<String> instruments){
		this.instrumentList = instruments;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(  
			     layoutInflater.getContext(), android.R.layout.simple_list_item_1,  
			     instruments);
		setListAdapter(adapter);
		getListView().setSelector(R.drawable.list_selector);
	};
}
