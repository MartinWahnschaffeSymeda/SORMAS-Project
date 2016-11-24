package de.symeda.sormas.backend.person;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.person.ApproximateAgeType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonFacade;
import de.symeda.sormas.api.person.PersonIndexDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper.Pair;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.backend.facility.FacilityService;
import de.symeda.sormas.backend.location.LocationFacadeEjb;
import de.symeda.sormas.backend.location.LocationFacadeEjb.LocationFacadeEjbLocal;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;

@Stateless(name = "PersonFacade")
public class PersonFacadeEjb implements PersonFacade {
	
	@EJB
	private PersonService service;
	@EJB
	private FacilityService facilityService;
	@EJB
	private LocationFacadeEjbLocal locationFacade;
	@EJB
	private UserService userService;

	
	@Override
	public List<PersonReferenceDto> getAllPersons() {

		return service.getAll().stream()
			.map(c -> toReferenceDto(c))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<PersonIndexDto> getIndexList(UserReferenceDto userRef) {
		
		User user = userService.getByReferenceDto(userRef);
		
		if (user == null) {
			return Collections.emptyList();
		}

		// TODO match User
		
		return service.getAllAfter(null).stream()
			.map(c -> toIndexDto(c))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<PersonReferenceDto> getAllPersonsAfter(Date date) {
		return service.getAllAfter(date).stream()
			.map(c -> toReferenceDto(c))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<PersonDto> getAllCasePersonsAfter(Date date) {
		List<PersonDto> result = service.getAllAfter(date).stream()
			.map(c -> toCasePersonDto(c))
			.collect(Collectors.toList());
		return result;
	}

	@Override
	public List<PersonReferenceDto> getAllNoCasePersons() {

		return service.getAllNoCase().stream()
				.map(c -> toReferenceDto(c))
				.collect(Collectors.toList());
	}

	@Override
	public PersonReferenceDto getReferenceByUuid(String uuid) {
		return Optional.of(uuid)
				.map(u -> service.getByUuid(u))
				.map(c -> toReferenceDto(c))
				.orElse(null);
	}
	
	@Override
	public PersonDto getPersonByUuid(String uuid) {
		return Optional.of(uuid)
				.map(u -> service.getByUuid(u))
				.map(c -> toCasePersonDto(c))
				.orElse(null);
	}
	
	@Override
	public PersonReferenceDto savePerson(PersonReferenceDto dto) {
		Person person = toPerson(dto);
		service.ensurePersisted(person);
		
		return toReferenceDto(person);
		
	}
	
	@Override
	public PersonDto savePerson(PersonDto dto) {
		Person person = fromCasePersonDto(dto);
		service.ensurePersisted(person);
		
		return toCasePersonDto(person);
		
	}
	
	public Person toPerson(@NotNull PersonReferenceDto dto) {
		Person bo = service.getByUuid(dto.getUuid());
		if(bo==null) {
			bo = service.createPerson();
			if (dto.getCreationDate() != null) {
				bo.setCreationDate(new Timestamp(dto.getCreationDate().getTime()));
			}
		}
		bo.setUuid(dto.getUuid());
		bo.setFirstName(dto.getFirstName());
		bo.setLastName(dto.getLastName());
		return bo;
	}
	
	public Person fromCasePersonDto(@NotNull PersonDto dto) {
		Person bo = service.getByUuid(dto.getUuid());
		if(bo==null) {
			bo = service.createPerson();
			bo.setUuid(dto.getUuid());
			if (dto.getCreationDate() != null) {
				bo.setCreationDate(new Timestamp(dto.getCreationDate().getTime()));
			}
		}
		
		// case uuid is ignored!
		
		bo.setFirstName(dto.getFirstName());
		bo.setLastName(dto.getLastName());
		bo.setSex(dto.getSex());
		
		bo.setPresentCondition(dto.getPresentCondition());
		bo.setBirthdateDD(dto.getBirthdateDD());
		bo.setBirthdateMM(dto.getBirthdateMM());
		bo.setBirthdateYYYY(dto.getBirthdateYYYY());
		bo.setApproximateAge(dto.getApproximateAge());
		bo.setApproximateAgeType(dto.getApproximateAgeType());
		bo.setDeathDate(dto.getDeathDate());
		bo.setDead(dto.getDeathDate()!=null);
		
		bo.setPhone(dto.getPhone());
		bo.setPhoneOwner(dto.getPhoneOwner());
		bo.setAddress(locationFacade.fromLocationDto(dto.getAddress()));
		
		bo.setOccupationType(dto.getOccupationType());
		bo.setOccupationDetails(dto.getOccupationDetails());
		bo.setOccupationFacility(facilityService.getByReferenceDto(dto.getOccupationFacility()));
		return bo;
	}
	
	public static PersonReferenceDto toReferenceDto(Person entity) {
		PersonReferenceDto dto = new PersonReferenceDto();
		DtoHelper.fillReferenceDto(dto, entity);

		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		
		return dto;
	}
	
	public static PersonIndexDto toIndexDto(Person entity) {
		PersonIndexDto dto = new PersonIndexDto();
		DtoHelper.fillReferenceDto(dto, entity);

		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		dto.setSex(entity.getSex());
		dto.setPresentCondition(entity.getPresentCondition());
		
		if (entity.getBirthdateYYYY() != null) {
			Calendar birthdate = new GregorianCalendar();
			birthdate.set(entity.getBirthdateYYYY(), entity.getBirthdateMM()!=null?entity.getBirthdateMM()-1:0, entity.getBirthdateDD()!=null?entity.getBirthdateDD():1);
			
			Pair<Integer, ApproximateAgeType> pair = DateHelper.getApproximateAge(
					birthdate.getTime(),
					entity.getDeathDate()
					);
			dto.setApproximateAge(pair.getElement0());
			dto.setApproximateAgeType(pair.getElement1());
		}
		else {
			dto.setApproximateAge(entity.getApproximateAge());
			dto.setApproximateAgeType(entity.getApproximateAgeType());
		}

		return dto;
	}
	
	public static PersonDto toCasePersonDto(Person entity) {
		PersonDto dto = new PersonDto();
		DtoHelper.fillReferenceDto(dto, entity);
		
		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		dto.setSex(entity.getSex());
		
		dto.setPresentCondition(entity.getPresentCondition());
		dto.setBirthdateDD(entity.getBirthdateDD());
		dto.setBirthdateMM(entity.getBirthdateMM());
		dto.setBirthdateYYYY(entity.getBirthdateYYYY());
		dto.setDeathDate(entity.getDeathDate());
		
		if (entity.getBirthdateYYYY() != null) {
			Calendar birthdate = new GregorianCalendar();
			birthdate.set(entity.getBirthdateYYYY(), entity.getBirthdateMM()!=null?entity.getBirthdateMM()-1:0, entity.getBirthdateDD()!=null?entity.getBirthdateDD():1);
			
			Pair<Integer, ApproximateAgeType> pair = DateHelper.getApproximateAge(
					birthdate.getTime(),
					entity.getDeathDate()
					);
			dto.setApproximateAge(pair.getElement0());
			dto.setApproximateAgeType(pair.getElement1());
		}
		else {
			dto.setApproximateAge(entity.getApproximateAge());
			dto.setApproximateAgeType(entity.getApproximateAgeType());
		}
		
		dto.setPhone(entity.getPhone());
		dto.setPhoneOwner(entity.getPhoneOwner());
		dto.setAddress(LocationFacadeEjb.toLocationDto(entity.getAddress()));
		
		dto.setOccupationType(entity.getOccupationType());
		dto.setOccupationDetails(entity.getOccupationDetails());
		dto.setOccupationFacility(DtoHelper.toReferenceDto(entity.getOccupationFacility()));
		return dto;
	}

}
