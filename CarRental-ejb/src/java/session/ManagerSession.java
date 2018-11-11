package session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.RentalStore;
import rental.Reservation;
import java.sql.*;
import javax.persistence.Query;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em; // container managed entity manager
    
    public List<CarRentalCompany> lookUpAllRentalCompanies() {
        return em.createQuery("SELECT company FROM CarRentalCompany company").getResultList();
    }
    
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        List<CarType> result;
        /*try {
            return new HashSet<CarType>(RentalStore.getRental(company).getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }*/
        Query query = em.createQuery("SELECT DISTINCT car.type FROM Car car, CarRentalCompany company "
                + "WHERE car MEMBER OF company.cars "
                + "AND company.name = :companyName")
                .setParameter("companyName", company);
        result = query.getResultList();
        return new HashSet<>(result);
    }

 /*   @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            return RentalStore.getRental(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }
*/
    @Override
    public void addCarRentalCompany(CarRentalCompany crc) {
        List<Car> carsAtNewCompany = crc.getCars();
        crc.setCars(new ArrayList<Car>());
        em.persist(crc);
        for (Car car : carsAtNewCompany) {
            addCarToCompany(crc, car);
        }
    }

    @Override
    public void addCarToCompany(CarRentalCompany crc, Car car) {
        CarType type = em.find(CarType.class , car.getType().getName());
        if (type != null) {
            car.setType(type);
        } else {
            crc.addCar(car);
        }
    }

    @Override
    public Set<String> getBestClients() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) throws Exception {
        Query query = em.createQuery("SELECT carType, COUNT(carType) AS total FROM Reservation reservation, CarType carType "
        + "WHERE reservation.rentalCompany = :companyName "
            +"AND carType.companyName = :companyName "
        + "GROUP BY carType "
        + "ORDER BY total DESC")
                .setParameter("companyName", carRentalCompanyName);
        //TODO DATUM in verwerken!!!!
        //TODO can zijn dat het List<CarType[]> moet zijn!
        List<CarType> result = query.getResultList();
        return (CarType) result.get(0);
    }

    @Override
    public int getNumberOfReservationsForCarType(String company, String type) {
        Query query = em.createQuery("SELECT reservation FROM Reservation reservation, Car car "
        + "WHERE reservation.rentalCompany = :companyName "
        + "AND car.type.name = :carTypeName "
        + "AND reservation MEMBER OF car.reservations")
                .setParameter("companyName", company)
                .setParameter("carTypeName", type);
        List<Reservation[]> result = query.getResultList();
        return result.get(0).length;
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Query query = em.createQuery("SELECT car.id FROM Car car, CarRentalCompany company "
        + "WHERE company.name = :companyName "
        + "AND car MEMBER OF company.cars"+ "AND car.type.name = :carTypeName")
                .setParameter("companyName", company)
                .setParameter("carTypeName", type);
        List<Integer> result = query.getResultList();
        return new HashSet<Integer>(result);
    }

    @Override
    public int getNumberOfReservations(String company, String type, int carId) {
        Query query = em.createQuery("SELECT reservation FROM Reservation reservation, CarRentalCompany company, Car car "
        + "WHERE company.name = :companyName "
        + "AND car.id = :carId "
        + "AND reservation MEMBER OF car.reservations")
                .setParameter("companyName", company)
                .setParameter("carId", carId);
        List<Reservation> result = query.getResultList();
        return result.size();
    }
    
    public int getNumberOfReservationsBy(String clientName) {
        Query query = em.createQuery("SELECT reservation FROM Reservation reservation "
        + "Where reservation.carRenter = :clientName")
                .setParameter("clientName", clientName);
        List<Reservation> result = query.getResultList();
        return result.size();
    }
       
    

}