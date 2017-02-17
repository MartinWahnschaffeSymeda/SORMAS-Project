package de.symeda.sormas.rest;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleFacade;

@Path("/samples")
@Produces({MediaType.APPLICATION_JSON + "; charset=UTF-8"})
@Consumes({MediaType.APPLICATION_JSON + "; charset=UTF-8"})
public class SampleResource {
	
	@GET
	@Path("/all/{user}/{since}")
	public List<SampleDto> getAllSamples(@PathParam("user") String userUuid, @PathParam("since") long since) {
		List<SampleDto> samples = FacadeProvider.getSampleFacade().getAllSamplesAfter(new Date(since), userUuid);
		return samples;
	}
	
	@POST
	@Path("/push")
	public Integer postSamples(List<SampleDto> dtos) {
		SampleFacade sampleFacade = FacadeProvider.getSampleFacade();
		for (SampleDto dto : dtos) {
			sampleFacade.saveSample(dto);
		}
		
		return dtos.size();
	}

}
