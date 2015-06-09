package com.wawoo.data;


public class Reminder {
		
		//private variables
		int _id;
		String _prog_name;
		long _time;
		int _channel_id;
		String _channel_desc;
		String _url;
		
		// Empty constructor
		public Reminder(){
			
		}
		// constructor
		public Reminder(String prog_name, long time,int ch_id,String channel_desc,String url){
			this._prog_name = prog_name;
			this._time = time;
			this._channel_id = ch_id;
			this._channel_desc = channel_desc;
			this._url = url;
		}
		public int get_channel_id() {
			return _channel_id;
		}
		public void set_channel_id(int _channel_id) {
			this._channel_id = _channel_id;
		}
		public String get_channel_desc() {
			return _channel_desc;
		}
		public void set_channel_desc(String _channel_desc) {
			this._channel_desc = _channel_desc;
		}
		public String get_url() {
			return _url;
		}
		public void set_url(String _url) {
			this._url = _url;
		}
		public int get_id() {
			return _id;
		}
		public void set_id(int _id) {
			this._id = _id;
		}
		public String get_prog_name() {
			return _prog_name;
		}
		public void set_prog_name(String _prog_name) {
			this._prog_name = _prog_name;
		}
		public long get_time() {
			return _time;
		}
		public void set_time(long _time) {
			this._time = _time;
		}
		
		
	}
