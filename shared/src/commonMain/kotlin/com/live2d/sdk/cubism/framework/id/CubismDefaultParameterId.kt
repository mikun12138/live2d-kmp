package com.live2d.sdk.cubism.framework.id

/**
 * Constant class that holds the default value of the parameter ID.
 *
 *
 * Default value specifications are based on the following manual.
 * [...](http://docs.live2d.com/cubism-editor-manual/standard-parametor-list/)
 */
object CubismDefaultParameterId {
    enum class HitAreaId(
        val id: String
    ) {
        PREFIX("HitArea"),
        HEAD("Head"),
        BODY("Body");
    }

    enum class PartId(
        val id: String
    ) {
        CORE("Parts01Core"),
        ARM_PREFIX("Parts01Arm_"),
        ARM_L_PREFIX("Parts01ArmL_"),
        ARM_R_PREFIX("Parts01ArmR_");
    }

    enum class ParameterId(
        val id: String
    ) {
        ANGLE_X("ParamAngleX"),
        ANGLE_Y("ParamAngleY"),
        ANGLE_Z("ParamAngleZ"),

        EYE_L_OPEN("ParamEyeLOpen"),
        EYE_L_SMILE("ParamEyeLSmile"),
        EYE_R_OPEN("ParamEyeROpen"),
        EYE_R_SMILE("ParamEyeRSmile"),
        EYE_W_L_Y("ParamBrowLY"),
        EYE_BALL_X("ParamEyeBallX"),
        EYE_BALL_Y("ParamEyeBallY"),
        EYE_BALL_FORM("ParamEyeBallForm"),

        BROW_L_X("ParamBrowLX"),
        BROW_L_Y("ParamBrowLY"),
        BROW_R_X("ParamBrowRX"),
        BROW_R_Y("ParamBrowRY"),
        BROW_L_ANGLE("ParamBrowLAngle"),
        BROW_R_ANGLE("ParamBrowRAngle"),
        BROW_L_FORM("ParamBrowLForm"),
        BROW_R_FORM("ParamBrowRForm"),

        MOUTH_FORM("ParamMouthForm"),
        MOUTH_OPEN_Y("ParamMouthOpenY"),

        CHEEK("ParamCheek"),

        BODY_ANGLE_X("ParamBodyAngleX"),
        BODY_ANGLE_Y("ParamBodyAngleY"),
        BODY_ANGLE_Z("ParamBodyAngleZ"),

        BREATH("ParamBreath"),

        ARM_L_A("ParamArmLA"),
        ARM_L_B("ParamArmLB"),
        ARM_R_A("ParamArmRA"),
        ARM_R_B("ParamArmRB"),

        HAND_L("ParamHandL"),
        HAND_R("ParamHandR"),

        HAIR_FRONT("ParamHairFront"),
        HAIR_SIDE("ParamHairSide"),
        HAIR_BACK("ParamHairBack"),
        HAIR_FLUFFY("ParamHairFluffy"),

        SHOULDER_Y("ParamShoulderY"),

        BUST_X("ParamBustX"),
        BUST_Y("ParamBustY"),

        BASE_X("ParamBaseX"),
        BASE_Y("ParamBaseY"),

        NONE("NONE");
    }
}