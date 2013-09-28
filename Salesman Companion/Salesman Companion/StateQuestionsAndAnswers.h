//
//  StateQuestionsAndAnswers.h
//  Salesman Companion
//
//  Created by Taylor McCord on 9/23/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface StateQuestionsAndAnswers : NSObject

@property(nonatomic)NSInteger responseId;
@property(nonatomic)NSInteger licenseId;
@property(nonatomic)NSInteger stateQuestionsSpecificsId;
@property(nonatomic, strong)NSString *responseText;
@property(nonatomic)NSInteger responseBool;
@property(nonatomic)NSInteger questionId;
@property(nonatomic)NSInteger stateQuestionId;
@property(nonatomic, strong)NSString *questionText;
@property(nonatomic)NSInteger responseType;
@property(nonatomic)NSInteger questionOrder;

+(NSArray *)parseList:(NSArray *)list;

@end
