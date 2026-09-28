package pt.unl.fct.di.syspriv.xacml;

import pt.unl.fct.di.syspriv.entities.Appointment;
import pt.unl.fct.di.syspriv.entities.Employee;
import pt.unl.fct.di.syspriv.entities.Patient;
import pt.unl.fct.di.syspriv.entities.Payment;

import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class XACMLPDP {

    private static final String SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    private static final String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    private static final String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    private static final String ENV_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";

    private final BasePdpEngine pdp;
    private final Logger logger;

    public XACMLPDP() throws Exception {
        PdpEngineConfiguration config = PdpEngineConfiguration.getInstance(
                XACMLPDP.class.getClassLoader().getResource("pdp.xml").getPath()
        );

        this.pdp = new BasePdpEngine(config);
        this.logger = LoggerFactory.getLogger(XACMLPDP.class);
    }

    private String loadPolicyXml(String policyFile) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(policyFile);

        if (is != null) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        if (Files.exists(Paths.get(policyFile))) {
            return new String(Files.readAllBytes(Paths.get(policyFile)), StandardCharsets.UTF_8);
        }

        throw new IllegalStateException("Policy file not found: " + policyFile);
    }

    public boolean checkAccess(Object requester, Object resource, String context, Map<String, String> extraEnvAttrs) throws Exception {
        Map<String, String> subjectAttrs = new HashMap<>();
        Map<String, String> resourceAttrs = new HashMap<>();
        Map<String, String> actionAttrs = new HashMap<>();
        Map<String, String> envAttrs = new HashMap<>();

        if (requester == null || resource == null || context == null) {
            throw new IllegalArgumentException("Requester, resource and context cannot be null");
        }

        // Mapeamento do Requerente (Subject)
        if (requester instanceof Patient) {
            Patient patient = (Patient) requester;
            subjectAttrs.put("id", patient.getStringId());
            subjectAttrs.put("role", "Patient");
        } else if (requester instanceof Employee) {
            Employee employee = (Employee) requester;
            subjectAttrs.put("id", employee.getStringId());
            subjectAttrs.put("role", employee.getRole());
        } else {
            subjectAttrs.put("role", requester.toString()); // Suporte para papéis passados em testes (ex: DPO, Auditor)
        }

        // Mapeamento do Recurso
        if (resource instanceof Patient) {
            Patient patient = (Patient) resource;
            resourceAttrs.put("id", patient.getStringId());
            resourceAttrs.put("resource-type", "PatientData");

            if (patient.getPrimaryDoctor() != null) {
                resourceAttrs.put("doctor-id", patient.getPrimaryDoctor().getStringId());
            }
            if (patient.getErasureRequested()) {
                resourceAttrs.put("erasure-requested", "true");
            }
            if (patient.getRetentionUntil() != null && patient.getRetentionUntil().isBefore(java.time.LocalDateTime.now())) {
                resourceAttrs.put("retention-expired", "true");
            }
        } else if (resource instanceof Map) {
            // Permite passar mapas diretos para testes avançados
            resourceAttrs.putAll((Map<String, String>) resource);
        }

        envAttrs.put("context", context);
        if (extraEnvAttrs != null) {
            envAttrs.putAll(extraEnvAttrs);
        }

        actionAttrs.put("action-id", "read"); // Padrão de ação de leitura

        DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(-1, -1);

        addAttributes(requestBuilder, SUBJECT_CATEGORY, subjectAttrs);
        addAttributes(requestBuilder, RESOURCE_CATEGORY, resourceAttrs);
        addAttributes(requestBuilder, ACTION_CATEGORY, actionAttrs);
        addAttributes(requestBuilder, ENV_CATEGORY, envAttrs);

        var request = requestBuilder.build(true);
        DecisionResult result = pdp.evaluate(request);

        return result.getDecision() == DecisionType.PERMIT;
    }

    private void addAttributes(
            DecisionRequestBuilder<?> requestBuilder,
            String category,
            Map<String, String> attrs
    ) {
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            AttributeFqn fqn = AttributeFqns.newInstance(
                    category,
                    Optional.empty(),
                    entry.getKey()
            );

            AttributeBag<?> bag = Bags.singletonAttributeBag(
                    StandardDatatypes.STRING,
                    new StringValue(entry.getValue())
            );

            requestBuilder.putNamedAttributeIfAbsent(fqn, bag);
        }
    }
}
