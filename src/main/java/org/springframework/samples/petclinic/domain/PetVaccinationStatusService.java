package org.springframework.samples.petclinic.domain;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.adapters.PetVaccinationService;
import org.springframework.samples.petclinic.adapters.VaccinnationRecord;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetVaccine;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class PetVaccinationStatusService {

	@Autowired
	private OwnerRepository ownerRepositorys;

	@Autowired
	private PetVaccinationService adapter;


	@WithSpan
	public void updateVaccinationStatus(List<Integer> petIds) {


		for (Integer petId : petIds) {
			var pet = ownerRepositorys.findById(petId).getPet(petId);

			try {
				var vaccinationRecords = this.adapter.allVaccines();
				for (VaccinnationRecord record : vaccinationRecords) {

					var recordInfo = this.adapter.VaccineRecord(record.recordId());
					if (recordInfo.petId() == pet.getId()) {
						var date = LocalDateTime.ofInstant(recordInfo.vaccineDate(), ZoneId.systemDefault());
						PetVaccine petVaccine = new PetVaccine();
						petVaccine.setDate(date.toLocalDate());
						pet.addVaccine(petVaccine);
					}
				}

			}
			catch (JSONException | IOException e) {
				// Fail silently
				Span.current().recordException(e);
			}

		}

	}

}
