//
//  StateQuestionsAndAnswers.m
//  Salesman Companion
//
//  Created by Taylor McCord on 9/23/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import "StateQuestionsAndAnswers.h"

@implementation StateQuestionsAndAnswers

+(NSArray *)parseList:(NSArray *)list{
    NSMutableArray *result = [[NSMutableArray alloc] initWithCapacity:list.count];
    for (NSDictionary *dictionary in list) {
        StateQuestionsAndAnswers *sqaa = [[StateQuestionsAndAnswers alloc] init];
        [sqaa setResponseId:[dictionary[@"responseId"]integerValue]];
        [sqaa setLicenseId:[dictionary[@"licenseId"]integerValue]];
        [sqaa setStateQuestionsSpecificsId:[dictionary[@"stateQuestionsSpecificsId"]integerValue]];
        [sqaa setResponseText:dictionary[@"responseText"]];
        [sqaa setResponseBool:[dictionary[@"responseBool"]integerValue]];
        [sqaa setQuestionId:[dictionary[@"questionId"]integerValue]];
        [sqaa setStateQuestionId:[dictionary[@"stateQuestionId"]integerValue]];
        [sqaa setQuestionText:dictionary[@"questionText"]];
        [sqaa setResponseType:[dictionary[@"responseType"]integerValue]];
        [sqaa setQuestionOrder:[dictionary[@"questionOrder"]integerValue]];
        [result addObject:sqaa];
    }
    return result;
}

@end
