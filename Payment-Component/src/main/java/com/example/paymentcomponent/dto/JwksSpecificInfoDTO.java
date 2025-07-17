package com.example.paymentcomponent.dto;

import java.util.List;

public class JwksSpecificInfoDTO {

    private final List<JwksKeyDTO> keys;

    public JwksSpecificInfoDTO(List<JwksKeyDTO> keys) {
        this.keys = keys;
    }

    public List<JwksKeyDTO> getKeys() {
        return keys;
    }

    public static class JwksKeyDTO {
        private final String kty;
        private final String kid;
        private final String alg;
        private final String n;
        private final String e;

        public JwksKeyDTO(String kty, String kid, String alg, String n, String e) {
            this.kty = kty;
            this.kid = kid;
            this.alg = alg;
            this.n = n;
            this.e = e;
        }

        public String getKty() {
            return kty;
        }

        public String getKid() {
            return kid;
        }

        public String getAlg() {
            return alg;
        }

        public String getN() {
            return n;
        }

        public String getE() {
            return e;
        }

        @Override
        public String toString() {
            return "JwksKeyDTO{" +
                    "kty='" + kty + '\'' +
                    ", kid='" + kid + '\'' +
                    ", alg='" + alg + '\'' +
                    ", n='" + n + '\'' +
                    ", e='" + e + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "JwksSpecificInfoDTO{" +
                "keys=" + keys +
                '}';
    }
}
