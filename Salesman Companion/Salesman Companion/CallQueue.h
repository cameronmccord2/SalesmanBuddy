//
//  CallQueue.h
//  Item Mapper
//
//  Created by Cameron McCord on 8/23/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CallQueue : NSObject{

}

@property(nonatomic)BOOL alreadySent;
@property(nonatomic, strong)id delegate;
@property(nonatomic, strong)NSURL *fullUrl;
@property(nonatomic, strong)NSDictionary *body;
@property(nonatomic, strong)NSNumber *type;

-(id)initQueueItem:(NSURL *)url type:(NSNumber *)newType body:(NSDictionary *)newBody delegate:(id)newDelegate;

@end
