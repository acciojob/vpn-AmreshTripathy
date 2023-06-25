package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();

        if (user.getMaskedIp() != null)
            throw new Exception("Already connected");
        else if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())) {
            return user;
        } else {
            if (user.getServiceProviderList() == null) {
                throw new Exception("Unable to connect");
            }

            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();

            int minId = Integer.MAX_VALUE;
            ServiceProvider serviceProvider = null;
            Country country = null;

            for (ServiceProvider sp : serviceProviderList) {
                for (Country country1 : sp.getCountryList()) {
                    if (country1.getCountryName().toString().equalsIgnoreCase(countryName) && minId > sp.getId()) {
                        minId = sp.getId();
                        serviceProvider = sp;
                        country = country1;
                    }
                }
            }

            if (serviceProvider != null) {

                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(serviceProvider);

                user.setMaskedIp(country.getCode() + "." + serviceProvider.getId() + "." + user.getId());
                user.setConnected(true);
                user.getConnectionList().add(connection);

                serviceProvider.getConnectionList().add(connection);

                userRepository2.save(user);
                serviceProviderRepository2.save(serviceProvider);

                return user;
            } else
                throw new Exception("Unable to connect");
        }
    }


    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (!user.getConnected())
            throw new Exception("Already disconnected");

        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        if (receiver.getMaskedIp() != null) {
            String Ip = receiver.getMaskedIp();

            String code = Ip.substring(0, 3);

            if (code.equals(sender.getOriginalCountry().getCode()))
                return sender;
            else {
                String countryName = "";

                if (code.equals(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();
                if (code.equals(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();
                if (code.equals(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();
                if (code.equals(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();
                if (code.equals(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();
                try {
                    User updatedSender = connect(senderId, countryName);
                    return updatedSender;
                } catch (Exception e) {
                    throw new Exception("Cannot establish communication");
                }
            }
        } else {
            if (receiver.getOriginalCountry().equals(sender.getOriginalCountry())) {
                return sender;
            } else {
                String countryName = receiver.getOriginalCountry().getCountryName().toString();
                try {
                    User updatedSender = connect(senderId, countryName);
                    return updatedSender;
                } catch (Exception e) {
                    throw new Exception("Cannot establish communication");
                }
            }
        }
    }
}