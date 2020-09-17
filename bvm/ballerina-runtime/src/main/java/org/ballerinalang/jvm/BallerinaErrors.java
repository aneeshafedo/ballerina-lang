/*
*   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypeIdSet;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;
import static org.ballerinalang.jvm.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.jvm.util.BLangConstants.START_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.STOP_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.995.0
 */
public class BallerinaErrors {

    public static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");
    public static final String NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String CALL_STACK_ELEMENT = "CallStackElement";
    public static final BString ERROR_CAUSE_FIELD = StringUtils.fromString("cause");
    public static final String ERROR_STACK_TRACE = "stackTrace";
    public static final String ERROR_PRINT_PREFIX = "error: ";
    public static final String GENERATE_PKG_INIT = "___init_";
    public static final String GENERATE_PKG_START = "___start_";
    public static final String GENERATE_PKG_STOP = "___stop_";
    public static final String GENERATE_OBJECT_CLASS_PREFIX = ".$value$";

    @Deprecated
    public static ErrorValue createError(String message) {
        return createError(StringUtils.fromString(message));
    }

    public static ErrorValue createError(BString message) {
        return new ErrorValue(message, new MapValueImpl<>(BTypes.typeErrorDetail).frozenCopy(new HashMap<>()));
    }

    @Deprecated
    public static ErrorValue createError(String reason, String detail) {
            return createError(StringUtils.fromString(reason), StringUtils.fromString(detail));
    }

    public static ErrorValue createError(BString reason, BString detail) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(reason, readonlyCloneDetailMap(detailMap));
    }

    @SuppressWarnings("unchecked")
    private static MapValueImpl<BString, Object> readonlyCloneDetailMap(MapValueImpl<BString, Object> detailMap) {
        return (MapValueImpl<BString, Object>) detailMap.frozenCopy(new HashMap<>());
    }

    @Deprecated
    public static ErrorValue createError(BType type, String message, String detail) {
        return createError(type, StringUtils.fromString(message), StringUtils.fromString(detail));
    }

    public static ErrorValue createError(BType type, BString message, BString detail) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(type, message, null, readonlyCloneDetailMap(detailMap));
    }

    public static ErrorValue createDistinctError(String typeIdName, BPackage typeIdPkg, String message) {
        return createDistinctError(typeIdName, typeIdPkg, message, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public static ErrorValue createDistinctError(String typeIdName, BPackage typeIdPkg, String message,
                                                 MapValue<BString, Object> detailRecord) {
        ErrorValue error = createError(message, (MapValue) detailRecord.frozenCopy(new HashMap<>()));
        setTypeId(typeIdName, typeIdPkg, error);
        return error;
    }

    public static ErrorValue createDistinctError(String typeIdName, BPackage typeIdPkg, String message,
                                                 ErrorValue cause) {
        MapValueImpl<Object, Object> details = new MapValueImpl<>(BTypes.typeErrorDetail);
        ErrorValue error = new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(),
                TypeChecker.getType(details)),
                StringUtils.fromString(message),
                cause,
                details.frozenCopy(new HashMap<>()));
        setTypeId(typeIdName, typeIdPkg, error);
        return error;
    }

    public static void setTypeId(String typeIdName, BPackage typeIdPkg, ErrorValue error) {
        BErrorType type = (BErrorType) error.getType();
        BTypeIdSet typeIdSet = new BTypeIdSet();
        typeIdSet.add(typeIdPkg, typeIdName, true);
        type.setTypeIdSet(typeIdSet);
    }

    @Deprecated
    public static ErrorValue createError(String message, MapValue detailMap) {
        return createError(StringUtils.fromString(message), detailMap);
    }

    public static ErrorValue createError(BString message, MapValue detailMap) {
        return new ErrorValue(message, detailMap.frozenCopy(new HashMap<>()));
    }

    public static ErrorValue createError(Throwable error) {
        if (error instanceof ErrorValue) {
            return (ErrorValue) error;
        }
        return createError(error.getMessage());
    }

    public static ErrorValue trapError(Throwable throwable) {
        // Used to trap and create error value for non error value exceptions. At the moment, we can trap
        // stack overflow exceptions in addition to error value.
        // In the future, if we need to trap more exception types, we need to check instance of each exception and
        // handle accordingly.
        ErrorValue error = createError(BallerinaErrorReasons.STACK_OVERFLOW_ERROR);
        error.setStackTrace(throwable.getStackTrace());
        return error;
    }

    public static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(BALLERINA_PREFIXED_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType));
    }

    public static ErrorValue createTypeCastError(Object sourceVal, BType targetType) {
        throw createError(BallerinaErrorReasons.TYPE_CAST_ERROR,
                          BLangExceptionHelper.getErrorMessage(RuntimeErrors.TYPE_CAST_ERROR,
                                                               TypeChecker.getType(sourceVal), targetType));

    }

    public static ErrorValue createBToJTypeCastError(Object sourceVal, String targetType) {
        throw createError(BallerinaErrorReasons.TYPE_CAST_ERROR,
                BLangExceptionHelper.getErrorMessage(RuntimeErrors.J_TYPE_CAST_ERROR,
                        TypeChecker.getType(sourceVal), targetType));
    }

    public static ErrorValue createNumericConversionError(Object inputValue, BType targetType) {
        throw createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                          BLangExceptionHelper.getErrorMessage(
                                  RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                  TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static ErrorValue createNumericConversionError(Object inputValue, BType inputType, BType targetType) {
        throw createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR, BLangExceptionHelper.getErrorMessage(
                RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION, inputType, inputValue, targetType));
    }

    static BString getErrorMessageFromDetail(MapValueImpl<BString, Object> detailMap) {
        return (BString) detailMap.get(ERROR_MESSAGE_FIELD);
    }

    public static ErrorValue createCancelledFutureError() {
        return createError(BallerinaErrorReasons.FUTURE_CANCELLED);
    }

    public static ErrorValue createNullReferenceError() {
        return createError(BallerinaErrors.NULL_REF_EXCEPTION);
    }

    /**
     * Create ballerian error using java exception for interop.
     * @param e java exception
     * @return ballerina error
     */
    public static ErrorValue createInteropError(Throwable e) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (e.getMessage() != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, StringUtils.fromString(e.getMessage()));
        }
        if (e.getCause() != null) {
            detailMap.put(ERROR_CAUSE_FIELD, createError(e.getCause().getClass().getName(), e.getCause().getMessage()));
        }

        return createError(e.getClass().getName(), detailMap);
    }

    public static Object handleResourceError(Object returnValue) {
        if (returnValue instanceof ErrorValue) {
            throw (ErrorValue) returnValue;
        }
        return returnValue;
    }

    public static ArrayValue generateCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        int index = 0;
        for (StackTraceElement stackFrame : stackTrace) {
            Optional<StackTraceElement> stackTraceElement =
                    BallerinaErrors.filterStackTraceElement(stackFrame, index++);
            if (stackTraceElement.isPresent()) {
                filteredStack.add(stackTraceElement.get());
            }
        }
        BType recordType = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT).getType();
        ArrayValue callStack = new ArrayValueImpl(new BArrayType(recordType));
        for (int i = 0; i < filteredStack.size(); i++) {
            callStack.add(i, getStackFrame(filteredStack.get(i)));
        }
        return callStack;
    }

    public static Optional<StackTraceElement> filterStackTraceElement(StackTraceElement stackFrame, int currentIndex) {
        String fileName = stackFrame.getFileName();

        int lineNo = stackFrame.getLineNumber();
        if (lineNo < 0) {
            return Optional.empty();
        }

        // Handle init function
        String className = stackFrame.getClassName();
        String methodName = stackFrame.getMethodName();
        if (className.equals(MODULE_INIT_CLASS_NAME)) {
            if (currentIndex == 0) {
                return Optional.empty();
            }

            switch (methodName) {
                case GENERATE_PKG_INIT:
                    methodName = INIT_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_START:
                    methodName = START_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_STOP:
                    methodName = STOP_FUNCTION_SUFFIX;
                    break;
                default:
                    return Optional.empty();
            }

            return Optional.of(new StackTraceElement(cleanupClassName(className), methodName, fileName,
                    stackFrame.getLineNumber()));

        }

        if (!fileName.endsWith(BLANG_SRC_FILE_SUFFIX)) {
            // Remove java sources for bal stacktrace if they are not extern functions.
            return Optional.empty();
        }

        return Optional.of(
                new StackTraceElement(cleanupClassName(className), methodName, fileName, stackFrame.getLineNumber()));
    }

    private static MapValue<BString, Object> getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return BallerinaValues.
                createRecord(BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT), values);
    }

    private static String cleanupClassName(String className) {
        return className.replace(GENERATE_OBJECT_CLASS_PREFIX, ".");
    }
}