//
//  LicenceTableViewController.h
//  Salesman Companion
//
//  Created by Taylor McCord on 9/18/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CallQueue.h"

@interface LicenceTableViewController : UITableViewController{
    NSMutableArray *myItems;
    NSMutableArray *callQueue;
    NSDecimalNumber *connectionNumber;
    NSMutableDictionary *dataFromConnectionByTag;
    NSMutableDictionary *connections;
    NSNumber *typeMyItems;
    CallQueue *authCall;
}

@property(nonatomic, strong)NSMutableArray *licenses;

@end
