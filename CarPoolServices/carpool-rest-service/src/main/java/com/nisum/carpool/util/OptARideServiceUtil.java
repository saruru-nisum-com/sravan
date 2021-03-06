package com.nisum.carpool.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nisum.carpool.data.domain.CarpoolRiderDetails;
import com.nisum.carpool.data.domain.Carpooldetails;
import com.nisum.carpool.data.util.Ride_Status;
import com.nisum.carpool.service.dto.OptRideDto;

public class OptARideServiceUtil {

	public static OptRideDto convertToOptRideDto(Carpooldetails carppol, int optedCount) {
		OptRideDto optRideDto = new OptRideDto();
		optRideDto.setDate(carppol.getFromDate());
		optRideDto.setTotalSeats(carppol.getNoofseats());
		optRideDto.setOptedSeats(optedCount);
		optRideDto.setStatus(carppol.getStatus());
		return optRideDto;
	}

	public static CarpoolRiderDetails acceptStatusList(CarpoolRiderDetails carpoolRiderDetails) {
		if (carpoolRiderDetails.getStatus() == Ride_Status.APPROVED.getValue()
				|| carpoolRiderDetails.getStatus() == Ride_Status.REQUESTED.getValue()) {
			return carpoolRiderDetails;
		}
		return null;
	}

	public static List<Carpooldetails> removeParentIdFromCarPoolList(List<Carpooldetails> carPoolsDataList,
			int parentId) {
		Iterator<Carpooldetails> itr = carPoolsDataList.iterator();
		while (itr.hasNext()) {
			if ((itr.next().getId().equals(parentId))) {
				itr.remove();
				break;
			}
		}
		return carPoolsDataList;
	}

	public static boolean findCpIdRiderEmailId(String emailId, List<CarpoolRiderDetails> carPoolRiderDetailsList) {
		// TODO Auto-generated method stub
		CarpoolRiderDetails carPoolRiderDetails = null;
		Iterator<CarpoolRiderDetails> itr = carPoolRiderDetailsList.iterator();
		while (itr.hasNext()) {
			carPoolRiderDetails = itr.next();
			if ((carPoolRiderDetails.getEmailid().equals(emailId))) {
				return true;
			}
		}
		return false;
	}

	public static List<CarpoolRiderDetails> filterAcceptStatus(List<CarpoolRiderDetails> carPoolRiderDetailsList) {
		CarpoolRiderDetails carPoolRiderDetails = null;
		Iterator<CarpoolRiderDetails> itr = carPoolRiderDetailsList.iterator();
		List<CarpoolRiderDetails> carPoolRiderDeatilsAcceptedStatusList = new ArrayList<CarpoolRiderDetails>();
		while (itr.hasNext()) {
			carPoolRiderDetails = itr.next();
			carPoolRiderDetails = OptARideServiceUtil.acceptStatusList(carPoolRiderDetails);
			carPoolRiderDeatilsAcceptedStatusList.add(carPoolRiderDetails);
		}
		return carPoolRiderDeatilsAcceptedStatusList;
	}

}
