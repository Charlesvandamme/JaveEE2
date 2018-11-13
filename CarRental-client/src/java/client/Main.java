package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.naming.InitialContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {

    public Main(String scriptFile) {
        super(scriptFile);
    }

    public static void main(String[] args) throws Exception {
      // TODO: use updated manager interface to load cars into companies
        Main main = new Main("trips");
        ManagerSessionRemote ms = main.getNewManagerSession("","");
        System.out.println("this carcompany" + ms);
	CrcData data = loadData("C:\\Documenten\\DistributedSystems_EE2\\CarRental-client\\src\\java\\hertz.csv");
        CarRentalCompany crc = new CarRentalCompany(data.name, data.regions, data.cars);
        ms.addCarRentalCompany(crc);
        //CrcData data2 = loadData("C:\\Documenten\\DistributedSystems_EE2\\CarRental-client\\src\\java\\dockx.csv");
        //crc = new CarRentalCompany(data2.name, data2.regions, data2.cars);
        //ms.addCarRentalCompany(crc);        
        main.run();
    }



    @Override
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestClients();
    }

    @Override
    protected String getCheapestCarType(CarRentalSessionRemote session, Date start, Date end, String region) throws Exception {
        return session.getCheapestCarType(start, end, region);
    }

    @Override
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        return ms.getMostPopularCarTypeIn(carRentalCompanyName, year);
    }

    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        CarRentalSessionRemote rs = (CarRentalSessionRemote) (new InitialContext()).lookup(CarRentalSessionRemote.class.getName());
        rs.setRenterName(name);
        return rs;
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception {
        ManagerSessionRemote ms = (ManagerSessionRemote) (new InitialContext()).lookup(ManagerSessionRemote.class.getName());
        return ms;
    }

    @Override
    protected void checkForAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        session.getAvailableCarTypes(start, end);
    }

    @Override
    protected void addQuoteToSession(CarRentalSessionRemote session, String name, Date start, Date end, String carType, String region) throws Exception {
        ReservationConstraints resCon = new ReservationConstraints(start, end, carType, region);
        //TODO check of name wel naam van crc is!!!
        session.createQuote(name, resCon);
    }

    @Override
    protected List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception {
        //TODO wat te doen met name??
        return session.confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception {
        return ms.getNumberOfReservationsBy(clientName);
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservationsForCarType(carRentalName, carType);
    }
    
    // added 
    public void addCarRentalCompany(ManagerSessionRemote ms, CarRentalCompany crc) {
        ms.addCarRentalCompany(crc);
    }
    
    public void addCarRentalCompany(ManagerSessionRemote ms, CarRentalCompany crc, Car car) {
        ms.addCarToCompany(crc, car);
    }
    

    
    
	public static CrcData loadData(String datafile)
			throws ReservationException, NumberFormatException, IOException {

		CrcData out = new CrcData();
		int nextuid = 0;

		// open file
		BufferedReader in = new BufferedReader(new FileReader(datafile));
		StringTokenizer csvReader;
		
		try {
			// while next line exists
			
			while (in.ready()) {
				String line = in.readLine();
				
				if (line.startsWith("#")) {
					// comment -> skip					
				} else if (line.startsWith("-")) {
					csvReader = new StringTokenizer(line.substring(1), ",");
					out.name = csvReader.nextToken();
					out.regions = Arrays.asList(csvReader.nextToken().split(":"));
				} else {
					// tokenize on ,
					csvReader = new StringTokenizer(line, ",");
					// create new car type from first 5 fields
					CarType type = new CarType(csvReader.nextToken(),
							Integer.parseInt(csvReader.nextToken()),
							Float.parseFloat(csvReader.nextToken()),
							Double.parseDouble(csvReader.nextToken()),
							Boolean.parseBoolean(csvReader.nextToken()));
					System.out.println(type);
					// create N new cars with given type, where N is the 5th field
					for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
						out.cars.add(new Car(nextuid++, type));
					}
				}
			}
		} finally {
			in.close();
		}

		return out;
	}
	 
		static class CrcData {
			public List<Car> cars = new LinkedList<Car>();
			public String name;
			public List<String> regions =  new LinkedList<String>();
		}
    
    
}