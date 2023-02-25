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
		TripBooking tripBooking=new TripBooking();
		List<Driver>driverList=driverRepository2.findAll();
		Driver driver=null;
		for(Driver x:driverList){
			if(driver==null){
				driver=x;
			}
			else if(x.getDriverId()<driver.getDriverId()&&x.getCab().isAvailable()==true){
				driver=x;
			}
		}
		if(driver==null)
			throw new Exception("No cab available!");
		driver.getCab().setAvailable(false);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setBill(driver.getCab().getPerKmRate()*distanceInKm);
		tripBooking.setTripStatus(TripStatus.CONFIRMED);
		Customer customer=customerRepository2.findById(customerId).get();
		List<TripBooking>tripBookingListForCustomer=customer.getTripBookingList();
		tripBookingListForCustomer.add(tripBooking);
		customer.setTripBookingList(tripBookingListForCustomer);


		List<TripBooking>tripBookingListForDriver=driver.getTripBookingList();
		tripBookingListForDriver.add(tripBooking);
		driver.setTripBookingList(tripBookingListForDriver);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		driverRepository2.save(driver);
		customerRepository2.save(customer);
		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setTripStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		Driver driver=tripBooking.getDriver();
		driver.getCab().setAvailable(true);
		driverRepository2.save(driver);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setTripStatus(TripStatus.COMPLETED);
		Driver driver=tripBooking.getDriver();
		driver.getCab().setAvailable(true);
		driverRepository2.save(driver);

	}
}
