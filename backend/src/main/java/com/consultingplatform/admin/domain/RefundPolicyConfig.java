package com.consultingplatform.admin.domain;

import java.util.List;

public class RefundPolicyConfig {
    private List<RefundTier> tiers;

    public List<RefundTier> getTiers() {
        return tiers;
    }

    public void setTiers(List<RefundTier> tiers) {
        this.tiers = tiers;
    }

    public static class RefundTier {
        private int hoursBefore;
        private double refundPercentage;

        public int getHoursBefore() {
            return hoursBefore;
        }

        public void setHoursBefore(int hoursBefore) {
            this.hoursBefore = hoursBefore;
        }

        public double getRefundPercentage() {
            return refundPercentage;
        }

        public void setRefundPercentage(double refundPercentage) {
            this.refundPercentage = refundPercentage;
        }
    }
}
