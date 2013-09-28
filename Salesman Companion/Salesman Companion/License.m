//
//  License.m
//  Salesman Companion
//
//  Created by Taylor McCord on 9/18/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import "License.h"
#import "StateQuestionsAndAnswers.h"

@implementation License
@synthesize id;
@synthesize photo;
@synthesize bucketId;
@synthesize created;
@synthesize stateQuestions;

+(NSArray *)parseList:(NSArray *)list{
    NSMutableArray *result = [[NSMutableArray alloc] initWithCapacity:list.count];
    for (NSDictionary *dictionary in list) {
        License *license = [[License alloc] init];
        [license setId:[dictionary[@"id"] integerValue]];
        [license setPhoto:dictionary[@"photo"]];
        [license setBucketId:[dictionary[@"bucketId"]integerValue]];
        [license setCreated:[NSDate dateWithTimeIntervalSince1970:[dictionary[@"created"] integerValue]]];
        [license setStateQuestions:[StateQuestionsAndAnswers parseList:dictionary[@"stateQuestions"]]];
        [result addObject:license];
    }
    return result;
}
@end
