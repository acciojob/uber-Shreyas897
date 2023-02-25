package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
//		TripBooking tripBooking=new TripBooking();
//		List<Driver>driverList=driverRepository2.findAll();
//		Driver driver=null;
//		for(Driver x:driverList){
//			if(driver==null){
//				driver=x;
//			}
//			else if(x.getDriverId()<driver.getDriverId() && x.getCab().getAvailable()){
//				driver=x;
//			}
//		}
//		if(driver==null)
//			throw new Exception("No cab available!");
//		driver.getCab().setAvailable(false);
//		tripBooking.setDistanceInKm(distanceInKm);
//		tripBooking.setFromLocation(fromLocation);
//		tripBooking.setToLocation(toLocation);
//		tripBooking.setBill(0);
//		tripBooking.setStatus(TripStatus.CONFIRMED);
//		Customer customer=customerRepository2.findById(customerId).get();
//		tripBooking.setCustomer(customer);
//		tripBooking.setDriver(driver);
//
//		List<TripBooking>tripBookingListForCustomer=customer.getTripBookingList();
//		tripBookingListForCustomer.add(tripBooking);
//		customer.setTripBookingList(tripBookingListForCustomer);
//		customerRepository2.save(customer);
//
//
//		List<TripBooking>tripBookingListForDriver=driver.getTripBookingList();
//		tripBookingListForDriver.add(tripBooking);
//		driver.setTripBookingList(tripBookingListForDriver);
//
//		driverRepository2.save(driver);
//
//		return tripBooking;
		TripBooking tripBooking = new TripBooking();
		Driver driver = null;

		// Filtering the driver who is free and with lowest ID
		List<Driver> allDrivers = driverRepository2.findAll();

		for(Driver driver1 : allDrivers){

			//checking if driver are available or not
			if(driver1.getCab().getAvailable()==Boolean.TRUE){
				if((driver ==null) || (driver1.getDriverId() < driver.getDriverId())){
					driver = driver1;
				}
			}
		}
		// if no drivers are Available throws an execption
		if(driver==null){
			throw new Exception("No cab available!");
		}

		// Before Saving Setting up all the attributes of Entity Layers
		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);

		int ratePerKm = driver.getCab().getPerKmRate();
		tripBooking.setBill(distanceInKm*10);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		// as the cab is booked now Driver is not Available therefore setting the status to FALSE
		driver.getCab().setAvailable(false);

		//Setting Bi direction mapping attributes
		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);

		// we dont need to do tripBookingRepository2.save(tripBooking) because it is the child of both
		// Customer and Driver and due to cascading effect it get automatically saved

		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.setBill(tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm());
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);

	}
}
