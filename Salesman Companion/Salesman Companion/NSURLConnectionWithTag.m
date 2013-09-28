//
//  NSURLConnectionWithTag.m
//  VP+ Book
//
//  Created by Taylor McCord on 8/19/13.
//  Copyright (c) 2013 Cameron McCord. All rights reserved.
//

#import "NSURLConnectionWithTag.h"

@implementation NSURLConnectionWithTag
@synthesize typeTag;
@synthesize finalDelegate;
@synthesize uniqueTag;

-(id)initWithRequest:(NSURLRequest *)request delegate:(id)delegate startImmediately:(BOOL)startImmediately typeTag:(NSNumber *)newTypeTag uniqueTag:(NSDecimalNumber *)newUniqueTag finalDelegate:(id)newFinalDelegate{
    // idea from http://www.isignmeout.com/multiple-nsurlconnections-viewcontroller/
    // Do check to see if we can reach the internet
    // if i cant, return nil;
    if (self = [super initWithRequest:request delegate:delegate startImmediately:startImmediately]) {
        typeTag = newTypeTag;
        finalDelegate = newFinalDelegate;
        uniqueTag = newUniqueTag;
    }
    return self;
}
@end
