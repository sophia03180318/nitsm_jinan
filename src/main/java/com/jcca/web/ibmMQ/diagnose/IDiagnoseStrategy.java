package com.jcca.web.ibmMQ.diagnose;


import com.jcca.web.ibmMQ.support.INamedServiceProvider;

public interface IDiagnoseStrategy extends INamedServiceProvider {
    boolean isHealthy(IDiagnosable paramIDiagnosable);
}


