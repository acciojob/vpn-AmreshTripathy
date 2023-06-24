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

        if (user.getConnected())
            throw new Exception("Already connected");
        else if (user.getOriginalCountry().getCountryName().name().equals(countryName))
            return user;
        else {
            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
            if (serviceProviderList.isEmpty())
                throw new Exception("Unable to connect");

            int minId = Integer.MAX_VALUE;
            ServiceProvider serviceProvider = null;
            Country country = null;

            for (ServiceProvider sp : serviceProviderList) {
                for (Country country1 : sp.getCountryList()) {
                    if (country1.getCountryName().name().equals(countryName) && minId > sp.getId()) {
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

                connection = connectionRepository2.save(connection);

                user.setMaskedIp(country.getCode() + "." + serviceProvider.getId() + "." + user.getId());
                user.setConnected(true);
                user.getConnectionList().add(connection);

                serviceProvider.getConnectionList().add(connection);

                serviceProviderRepository2.save(serviceProvider);

                return userRepository2.save(user);
            } else {
                throw new Exception("Unable to connect");
            }
        }
    }


    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (!user.getConnected())
            throw new Exception("Already disconnected");

        user.setMaskedIp(null);
        user.setConnected(false);

        return userRepository2.save(user);
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        if (receiver.getMaskedIp().isEmpty()) {
            if (receiver.getOriginalCountry().equals(sender.getOriginalCountry()))
                return sender;
            else {
                try {
                    return connect(senderId, receiver.getOriginalCountry().getCountryName().name());
                } catch (Exception ex) {
                    throw new Exception("Cannot establish communication");
                }
            }
        } else {
            String code = receiver.getMaskedIp().substring(0, 3);
            if (sender.getOriginalCountry().getCode().equals(code))
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
                    return connect(senderId, countryName);
                } catch (Exception e) {
                    throw new Exception("Cannot establish communication");
                }
            }
        }
    }
}
