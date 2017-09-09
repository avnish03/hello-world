package com.ebizon.appify.shopifyapp;

/**
 * Created by avnish on 6/7/17.
 */
public class RecurringShopifyAppCharge {

    private static  final String recurring_application_charge = "recurring_application_charge";
    private static  final String basicPlan = "BasicSubscription";
    private static  final String smallBusiness = "SmallBusiness";
    private static  final String professionalSubscription = "ProfessionalSubscription";
    private static  final long basicPlanCharge = 59;
    private static  final long smallBusinessCharge = 199;
    private static  final long professionalSubscriptionCharge = 399;

    public static long getPlanPrice(String plan) {
        if(plan.equalsIgnoreCase(basicPlan))
            return basicPlanCharge;
        else if(plan.equalsIgnoreCase(smallBusiness))
            return smallBusinessCharge;
        else if(plan.equalsIgnoreCase(professionalSubscription))
            return professionalSubscriptionCharge;
        else
            return 0;
    }
}
