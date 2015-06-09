package com.wawoo.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceDatum implements Parcelable,Comparable<ServiceDatum> {

	private Integer serviceId;

	private Integer clientId;

	private String channelName;
	
	private String channelDescription;

	private String category;
	
	private String subCategory;
	
	private String image;

	private String url;

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getChannelDescription() {
		return channelDescription;
	}

	public void setChannelDescription(String channelDescription) {
		this.channelDescription = channelDescription;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeIntArray(new int[] { this.serviceId, this.clientId });
		dest.writeStringArray(new String[] {
				//this.channelName,
				this.channelDescription, this.image,
				this.url });
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public ServiceDatum createFromParcel(Parcel in) {
			return new ServiceDatum(in);
		}

		public ServiceDatum[] newArray(int size) {
			return new ServiceDatum[size];
		}
	};

	public ServiceDatum(){}
	
	// Parcelling part
	public ServiceDatum(Parcel in) {
		int[] intData = new int[2];
		String[] stringData = new String[3];

		in.readIntArray(intData);
		this.serviceId = intData[0];
		this.clientId = intData[1];

		in.readStringArray(stringData);
		//this.channelName = stringData[0];
		this.channelDescription = stringData[1];
		this.image = stringData[2];
		this.url = stringData[3];
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}


	@Override
	public int compareTo(ServiceDatum another) {
		return this.channelDescription.compareToIgnoreCase(another.getChannelDescription());
	}

}
