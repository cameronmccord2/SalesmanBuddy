//
//  License.h
//  Salesman Companion
//
//  Created by Taylor McCord on 9/18/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface License : NSObject

@property(nonatomic)NSInteger id;
@property(nonatomic, strong)NSString *photo;
@property(nonatomic)NSInteger bucketId;
@property(nonatomic, strong)NSDate *created;
@property(nonatomic, strong)NSArray *stateQuestions;

+(NSArray *)parseList:(NSArray *)list;

@end
