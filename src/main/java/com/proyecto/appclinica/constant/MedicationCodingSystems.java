package com.proyecto.appclinica.constant;

/**
 * Constantes para los sistemas de codificación de medicamentos
 */
public class MedicationCodingSystems {

    /** Sistema RxNorm para la codificación de medicamentos */
    public static final String RXNORM = "http://www.nlm.nih.gov/research/umls/rxnorm";

    /** Sistema SNOMED CT para la codificación de medicamentos */
    public static final String SNOMED_CT = "http://snomed.info/sct";

    /** Sistema ATC (Anatomical Therapeutic Chemical) para la codificación de medicamentos */
    public static final String ATC = "http://www.whocc.no/atc";

    /** Sistema NDC (National Drug Code) para la codificación de medicamentos */
    public static final String NDC = "http://hl7.org/fhir/sid/ndc";

    private MedicationCodingSystems() {
        // Constructor privado para evitar la instanciación
    }
}
